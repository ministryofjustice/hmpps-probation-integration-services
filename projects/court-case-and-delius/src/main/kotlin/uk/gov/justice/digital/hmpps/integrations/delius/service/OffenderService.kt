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
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Disability as DisabilityEntity

@Service
class OffenderService(
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

    fun getOffenderDetailSummary(crn: String): OffenderDetailSummary {
        val person = personRepository.getPerson(crn)
        val previousConviction = documentRepository.getPreviousConviction(person.id)
        return person.toOffenderSummary(previousConviction)
    }

    fun getOffenderDetail(crn: String): OffenderDetail {
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

fun Person.toOffenderSummary(previousConviction: DocumentEntity?) = OffenderDetailSummary(
    preferredName = preferredName,
    activeProbationManagedSentence = currentDisposal,
    contactDetails = ContactDetailsSummary(
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
    offenderProfile = toProfile(previousConviction),
    otherIds = toOtherIds(),
    partitionArea = partitionArea.area,
    previousSurname = previousSurname,
    softDeleted = softDeleted,
    title = title?.description
)

fun Person.toContactDetails() = ContactDetails(
    allowSMS = allowSms,
    emailAddresses = listOfNotNull(emailAddress),
    phoneNumbers = listOf(
        PhoneNumber(telephoneNumber, PhoneTypes.TELEPHONE.name),
        PhoneNumber(mobileNumber, PhoneTypes.MOBILE.name)
    ),
    addresses = addresses.map { it ->
        Address(
            from = it.startDate,
            to = it.endDate,
            noFixedAbode = it.noFixedAbode,
            notes = it.notes,
            addressNumber = it.addressNumber,
            buildingName = it.buildingName,
            streetName = it.streetName,
            district = it.district,
            town = it.town,
            county = it.county,
            postcode = it.postcode,
            telephoneNumber = it.telephoneNumber,
            status = it.status.keyValueOf(),
            type = it.type.keyValueOf(),
            typeVerified = it.typeVerified,
            latestAssessmentDate = it.addressAssessments.map { it.assessmentDate }.maxByOrNull { it },
            createdDatetime = it.createdDatetime,
            lastUpdatedDatetime = it.lastUpdatedDatetime
        )
    }
)

fun Person.toOtherIds() = OtherIds(
    crn = crn,
    croNumber = croNumber,
    immigrationNumber = immigrationNumber,
    mostRecentPrisonerNumber = mostRecentPrisonerNumber,
    niNumber = niNumber,
    nomsNumber = nomsNumber,
    pncNumber = pnc
)

fun Person.toOffenderManagers() = offenderManagers.sortedByDescending { it.date }.map { it ->
    OffenderManager(
        trustOfficer = Human(
            forenames = listOfNotNull(it.officer.forename, it.officer.forename2).joinToString(" "),
            surname = it.officer.surname
        ),
        softDeleted = it.softDeleted,
        partitionArea = it.partitionArea.area,
        staff = it.staff?.let { staff ->
            StaffHuman(
                code = staff.code,
                forename = staff.forename,
                surname = staff.surname,
                isUnallocated = staff.isUnallocated()
            )
        },
        providerEmployee = it.providerEmployee.let { emp ->
            emp?.surname?.let { surname ->
                Human(
                    forenames = listOfNotNull(emp.forename, emp.forename2).joinToString(" "),
                    surname = surname
                )
            }
        },
        team = it.team?.let { team ->
            Team(
                code = team.code,
                description = team.description,
                telephone = team.telephone,
                emailAddress = team.emailAddress,
                localDeliveryUnit = KeyValue(team.ldu.code, team.ldu.description),
                district = KeyValue(team.district.code, team.district.description),
                borough = KeyValue(team.district.borough.code, team.district.borough.code),
                startDate = team.startDate,
                endDate = team.endDate,
                teamType = KeyValue(team.code, team.description)
            )
        },
        probationArea = ProbationArea(
            probationAreaId = it.provider.id,
            code = it.provider.code,
            description = it.provider.description,
            nps = it.provider.privateSector,
            institution = it.provider.institution?.toInstitution(),
            organisation = KeyValue(it.provider.organisation.code, it.provider.organisation.description)
        ),
        active = it.active,
        fromDate = it.date.toLocalDate(),
        toDate = it.endDate,
        allocationReason = it.allocationReason.keyValueOf()
    )
}

fun Person.toOffenderDetail(previousConviction: DocumentEntity?) = OffenderDetail(
    preferredName = preferredName,
    activeProbationManagedSentence = currentDisposal,
    contactDetails = toContactDetails(),
    offenderAliases = offenderAliases.map {
        OffenderAlias(
            id = it.aliasID,
            dateOfBirth = it.dateOfBirth,
            firstName = it.firstName,
            middleNames = listOfNotNull(it.secondName, it.thirdName),
            surname = it.surname,
            gender = it.gender.description
        )
    },
    currentDisposal = if (currentDisposal) "1" else "0",
    currentExclusion = currentExclusion,
    exclusionMessage = exclusionMessage,
    restrictionMessage = restrictionMessage,
    currentRestriction = currentRestriction,
    dateOfBirth = dateOfBirth,
    firstName = forename,
    middleNames = listOfNotNull(secondName, thirdName),
    surname = surname,
    gender = gender.description,
    offenderId = id,
    offenderProfile = toProfile(previousConviction),
    otherIds = toOtherIds(),
    offenderManagers = toOffenderManagers(),
    partitionArea = partitionArea.area,
    previousSurname = previousSurname,
    softDeleted = softDeleted,
    title = title?.description,
    currentTier = currentTier?.description
)

fun Person.toProfile(previousConviction: DocumentEntity?) = OffenderProfile(
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

)

fun Disposal.sentenceOf() = Sentence(
    disposalType.description,
    entryLength,
    entryLengthUnit?.description,
    lengthInDays,
    terminationDate,
    startDate,
    endDate?.toLocalDate(),
    terminationReason?.description
)
