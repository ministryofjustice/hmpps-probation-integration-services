package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.overview.Order
import uk.gov.justice.digital.hmpps.api.model.overview.Rar
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.api.model.sentence.Offence
import uk.gov.justice.digital.hmpps.api.model.sentence.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.CourtDocumentDetails
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AdditionalSentenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AdditionalSentence as ExtraSentence

@Service
class SentenceService(
    private val eventRepository: EventSentenceRepository,
    private val courtAppearanceRepository: CourtAppearanceRepository,
    private val additionalSentenceRepository: AdditionalSentenceRepository,
    private val personRepository: PersonRepository,
    private val requirementRepository: RequirementRepository,
    private val documentRepository: DocumentRepository
) {
    fun getMostRecentActiveEvent(crn: String): SentenceOverview {
        val person = personRepository.getSummary(crn)
        val (activeEvents, inactiveEvents) = eventRepository.findSentencesByPersonId(person.id).partition { it.active }

        return SentenceOverview(
            name = person.toName(),
            sentences = activeEvents.map {
                val courtAppearance = courtAppearanceRepository.getFirstCourtAppearanceByEventIdOrderByDate(it.id)
                val additionalSentences = additionalSentenceRepository.getAllByEventId(it.id)
                it.toSentence(courtAppearance, additionalSentences, crn)
            },
            ProbabtionHistory(inactiveEvents.count(), inactiveEvents.count { it.inBreach })
        )
    }

    fun Event.toSentence(courtAppearance: CourtAppearance?, additionalSentences: List<ExtraSentence>, crn: String) =
        Sentence(
            OffenceDetails(
                eventNumber = eventNumber,
                offence = mainOffence?.let { Offence(it.offence.description, it.offenceCount) },
                dateOfOffence = mainOffence?.date,
                notes = notes,
                additionalOffences = additionalOffences.map {
                    Offence(description = it.offence.description, count = it.offenceCount ?: 0)
                }
            ),
            Conviction(
                sentencingCourt = courtAppearance?.court?.name,
                responsibleCourt = court?.name,
                convictionDate = convictionDate,
                additionalSentences.map { it.toAdditionalSentence() }
            ),
            order = disposal?.toOrder(),
            requirements = requirementRepository.getRequirements(crn, eventNumber).map { it.toRequirement() },
            courtDocuments = documentRepository.getCourtDocuments(id, eventNumber).map { it.toCourtDocument() }
        )

    fun ExtraSentence.toAdditionalSentence(): AdditionalSentence =
        AdditionalSentence(length, amount, notes, type.description)

    fun PersonSummaryEntity.toName() =
        Name(forename, secondName, surname)

    fun Disposal.toOrder() =
        Order(description = type.description, length = length, startDate = date, endDate = expectedEndDate())

    fun RequirementDetails.toRequirement() = Requirement(
        description,
        codeDescription,
        length,
        notes,
        getRar(id)
    )

    private fun getRar(requirementId: Long): Rar {
        val rarDays = requirementRepository.getRarDaysByRequirementId(requirementId)
        val scheduledDays = rarDays.find { it.type == "SCHEDULED" }?.days ?: 0
        val completedDays = rarDays.find { it.type == "COMPLETED" }?.days ?: 0
        return Rar(completed = completedDays, scheduled = scheduledDays)
    }

    fun CourtDocumentDetails.toCourtDocument(): CourtDocument = CourtDocument(id, lastSaved, documentName)
}