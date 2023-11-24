package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Breach
import uk.gov.justice.digital.hmpps.api.model.Conviction
import uk.gov.justice.digital.hmpps.api.model.CourtReport
import uk.gov.justice.digital.hmpps.api.model.DocumentType
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.api.model.LicenceCondition
import uk.gov.justice.digital.hmpps.api.model.Offence
import uk.gov.justice.digital.hmpps.api.model.OffenderDocumentDetail
import uk.gov.justice.digital.hmpps.api.model.ProbationRecord
import uk.gov.justice.digital.hmpps.api.model.PssRequirement
import uk.gov.justice.digital.hmpps.api.model.ReportAuthor
import uk.gov.justice.digital.hmpps.api.model.Requirement
import uk.gov.justice.digital.hmpps.api.model.Sentence
import uk.gov.justice.digital.hmpps.api.model.keyValueOf
import uk.gov.justice.digital.hmpps.api.model.toOffenderManager
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Document
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.typeDescription
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtReportRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.AdditionalOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.MainOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.PssRequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.PersonManagerRepository

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
    fun getProbationRecord(crn: String): ProbationRecord {
        val person =
            personRepository.findByCrnAndSoftDeletedIsFalse(crn) ?: throw NotFoundException("Person", "crn", crn)
        val personManager =
            personManagerRepository.findActiveManager(person.id) ?: throw NotFoundException("PersonManager", "crn", crn)
        val documents = documentRepository.getPersonAndEventDocuments(person.id)
        val convictions = getConvictions(person, documents)
        return ProbationRecord(crn, listOf(personManager.toOffenderManager()), convictions)
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
                    listOfNotNull(m.staff.forename, m.staff.forename).joinToString(" "),
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
