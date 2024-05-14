package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import software.amazon.awssdk.utils.ImmutableMap
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.api.model.LicenceCondition
import uk.gov.justice.digital.hmpps.api.model.Offence
import uk.gov.justice.digital.hmpps.api.model.Requirement
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtReportRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.PssRequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.repository.PersonManagerRepository
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Disability as DisabilityEntity

@Service
class OffenderService(
    private val personManagerRepository: PersonManagerRepository,
    private val eventRepository: EventRepository,
    private val mainOffenceRepository: MainOffenceRepository,
    private val additionalOffenceRepository: AdditionalOffenceRepository,
    private val licenseConditionRepository: LicenceConditionRepository,
    private val requirementRepository: RequirementRepository,
    private val documentRepository: DocumentRepository,
    private val personRepository: PersonRepository,
    private val nsiRepository: NsiRepository,
    private val pssRequirementRepository: PssRequirementRepository,
    private val courtReportRepository: CourtReportRepository
) {

    fun getOffenderDetail(crn: String): OffenderDetailSummary {
        val person = personRepository.getPerson(crn)
        val previousConviction = documentRepository.getPreviousConviction(person.id)
        return person.toOffenderDetail(previousConviction)
    }

    private fun getOffences(event: Event): List<Offence> {
        val mainOffence =
            listOf(mainOffenceRepository.findByEvent(event).let { Offence(it.offence.description, true, it.date) })
        val additionalOffences = additionalOffenceRepository.findByEvent(event).map {
            Offence(it.offence.description, false, it.date)
        }
        return mainOffence + additionalOffences
    }

    fun getConvictions(person: Person, documents: List<Document>): List<Conviction> {
        val events = eventRepository.findAllByPerson(person)

        return events.map { event ->
            Conviction(
                event.active,
                event.inBreach,
                eventRepository.awaitingPSR(event.id) == 1,
                event.convictionDate,
                getOffences(event),
                event.disposal?.sentenceOf(),
                custodialType = event.disposal?.custody?.let { c -> KeyValue(c.status.code, c.status.description) },
                documents = getConvictionDocuments(event, documents),
                breaches = getBreaches(event),
                requirements = getRequirements(event.disposal),
                licenceConditions = getLicenseConditions(event.disposal),
                pssRequirements = getPssRequirements(event.disposal?.custody?.id),
                courtReports = getCourtReports(event)
            )
        }
    }

    private fun getCourtReports(event: Event) = courtReportRepository.getAllByEvent(event).map {
        CourtReport(
            it.dateRequested,
            it.dateRequired,
            it.dateCompleted,
            it.courtReportType?.keyValueOf(),
            it.deliveredCourtReportType?.keyValueOf(),
            author = it.reportManagers.lastOrNull()?.let { m ->
                ReportAuthor(
                    m.staff.isUnallocated(),
                    listOfNotNull(m.staff.forename, m.staff.forename2).joinToString(" "),
                    m.staff.surname
                )
            }
        )
    }

    private fun getPssRequirements(custodyId: Long?) = if (custodyId == null) {
        listOf()
    } else {
        pssRequirementRepository.findAllByCustodyId(custodyId).map {
            PssRequirement(it.mainCategory?.description, it.subCategory?.description)
        }
    }

    private fun getBreaches(event: Event) = nsiRepository.findAllBreachNSIByEventId(event.id).map {
        val description = if (it.subType?.description == null) it.type.description else it.subType.description
        Breach(description, it.outcome?.description, it.actualStartDate, it.statusDate)
    }

    private fun getConvictionDocuments(event: Event, documents: List<Document>) =
        documents.filter { it.eventId == event.id }.map {
            OffenderDocumentDetail(
                it.name,
                it.author,
                DocumentType.valueOf(it.type),
                it.description,
                it.createdAt?.atZone(EuropeLondon),
                KeyValue(it.tableName, it.typeDescription())
            )
        }

    private fun getRequirements(disposal: Disposal?) =
        if (disposal != null) {
            requirementRepository.getAllByDisposal(disposal).map {
                Requirement(
                    it.commencementDate,
                    it.terminationDate,
                    it.expectedStartDate,
                    it.expectedEndDate,
                    it.active,
                    it.mainCategory?.keyValueOf(),
                    it.subCategory?.keyValueOf(),
                    it.adMainCategory?.keyValueOf(),
                    it.adSubCategory?.keyValueOf(),
                    it.terminationReason?.keyValueOf(),
                    it.length
                )
            }
        } else {
            listOf()
        }

    private fun getLicenseConditions(disposal: Disposal?) =
        if (disposal != null) {
            licenseConditionRepository.getAllByDisposal(disposal)
                .map {
                    LicenceCondition(
                        it.mainCategory.description,
                        it.subCategory?.description,
                        it.startDate,
                        it.notes,
                        it.active
                    )
                }
        } else {
            listOf()
        }
}

fun DisabilityEntity.isActive(): Boolean {
    if (startDate.isAfter(LocalDate.now())) {
        return false
    }
    return finishDate == null || finishDate.isAfter(LocalDate.now())
}

fun Person.toOffenderDetail(previousConviction: DocumentEntity?) = OffenderDetailSummary(
    preferredName = preferredName,
    activeProbationManagedSentence = currentDisposal,
    contactDetails = ContactDetails(
        allowSMS = allowSms,
        emailAddresses = listOfNotNull(emailAddress),
        phoneNumbers = listOf(
            PhoneNumber(telephoneNumber, PhoneTypes.TELEPHONE.name),
            PhoneNumber(mobileNumber, PhoneTypes.MOBILE.name)
        )
    ),
    currentDisposal = if (currentDisposal) "1" else "0",
    currentExclusion = currentExclusion,
    currentRestriction = currentRestriction,
    dateOfBirth = dateOfBirth,
    firstName = forename,
    middleNames = listOfNotNull(secondName, thirdName),
    surname = surname,
    gender = gender.description,
    offenderId = id,
    offenderProfile = OffenderProfile(
        genderIdentity = genderIdentity?.description,
        selfDescribedGenderIdentity = genderIdentityDescription ?: genderIdentity?.description,
        disabilities = disabilities.sortedByDescending { it.startDate }.map {
            Disability(
                lastUpdatedDateTime = it.lastUpdated,
                disabilityCondition = KeyValue(it.condition.code, it.condition.description),
                disabilityId = it.id,
                disabilityType = KeyValue(it.type.code, it.type.description),
                endDate = it.finishDate,
                isActive = it.isActive(),
                notes = it.notes,
                provisions = emptyList(),
                startDate = it.startDate
            )
        },
        ethnicity = ethnicity?.description,
        immigrationStatus = immigrationStatus?.description,
        nationality = nationality?.description,
        offenderDetails = offenderDetails,
        offenderLanguages = OffenderLanguages(
            languageConcerns = languageConcerns,
            primaryLanguage = language?.description,
            requiresInterpreter = requiresInterpreter,
            otherLanguages = emptyList()
        ),
        previousConviction = previousConviction?.lastUpdated.let {
            previousConviction?.createdAt?.toLocalDate()
                ?.let { it1 ->
                    PreviousConviction(
                        convictionDate = it1,
                        detail = ImmutableMap.of("documentName", previousConviction.name)
                    )
                }
        },
        provisions = provisions.sortedByDescending { it.startDate }.map {
            Provision(
                category = it.category?.let { cat -> KeyValue(cat.code, cat.description) },
                finishDate = it.finishDate,
                notes = it.notes,
                provisionId = it.id,
                provisionType = KeyValue(it.type.code, it.type.description),
                startDate = it.startDate
            )
        },
        religion = religion?.description,
        remandStatus = currentRemandStatus,
        riskColour = currentHighestRiskColour,
        sexualOrientation = sexualOrientation?.description,
        secondaryNationality = secondNationality?.description
    ),
    otherIds = OtherIds(
        crn = crn,
        croNumber = croNumber,
        immigrationNumber = immigrationNumber,
        mostRecentPrisonerNumber = mostRecentPrisonerNumber,
        niNumber = niNumber,
        nomsNumber = nomsNumber,
        pncNumber = pnc
    ),
    partitionArea = partitionArea.area,
    previousSurname = previousSurname,
    softDeleted = softDeleted,
    title = title?.description
)

fun Disposal.sentenceOf() = Sentence(
    disposalType.description,
    entryLength,
    entryLengthUnit?.description,
    lengthInDays,
    terminationDate?.toLocalDate(),
    startDate.toLocalDate(),
    endDate?.toLocalDate(),
    terminationReason?.description
)
