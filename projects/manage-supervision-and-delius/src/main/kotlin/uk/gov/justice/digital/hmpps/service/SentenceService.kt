package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.overview.Order
import uk.gov.justice.digital.hmpps.api.model.overview.Rar
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.api.model.sentence.Offence
import uk.gov.justice.digital.hmpps.api.model.sentence.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.CourtDocumentDetails
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.Duration
import java.time.LocalDate
import kotlin.time.toKotlinDuration
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AdditionalSentence as ExtraSentence

@Service
class SentenceService(
    private val eventRepository: EventSentenceRepository,
    private val courtAppearanceRepository: CourtAppearanceRepository,
    private val additionalSentenceRepository: AdditionalSentenceRepository,
    private val personRepository: PersonRepository,
    private val requirementRepository: RequirementRepository,
    private val documentRepository: DocumentRepository,
    private val offenderManagerRepository: OffenderManagerRepository,
    private val upwAppointmentRepository: UpwAppointmentRepository,
    private val licenceConditionRepository: LicenceConditionRepository
) {
    fun getEvents(crn: String, eventNumber: String?): SentenceOverview {
        val person = personRepository.getPerson(crn)
        val activeEvents = eventRepository.findSentencesByPersonId(person.id).filter {
            it.active
        }

        return SentenceOverview(
            personSummary = person.toSummary(),
            activeEvents.map { it.toSentenceSummary() },
            sentence = activeEvents.firstOrNull {
                when (eventNumber) {
                    null -> true
                    else -> eventNumber == it.eventNumber
                }
            }?.toSentence(crn)
        )
    }

    fun getProbationHistory(crn: String): History {
        val person = personRepository.getPerson(crn)
        val (activeEvents, inactiveEvents) = eventRepository.findSentencesByPersonId(person.id).partition { it.active }

        return History(
            personSummary = person.toSummary(),
            activeEvents.map { it.toSentenceSummary() },
            ProbationHistory(
                inactiveEvents.count(),
                getMostRecentTerminatedDateFromInactiveEvents(inactiveEvents),
                inactiveEvents.count { it.inBreach },
                offenderManagerRepository.countOffenderManagersByPerson(person)
            )
        )
    }

    fun Event.toSentenceSummary() = SentenceSummary(
        eventNumber,
        disposal?.type?.description ?: "Pre-Sentence"
    )

    fun Event.toSentence(crn: String): Sentence {
        val courtAppearance = courtAppearanceRepository.getFirstCourtAppearanceByEventIdOrderByDate(id)
        val additionalSentences = additionalSentenceRepository.getAllByEventId(id)

        return Sentence(
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
            requirements = requirementRepository.getRequirements(id, eventNumber)
                .map { it.toRequirement() },
            courtDocuments = documentRepository.getCourtDocuments(id, eventNumber).map { it.toCourtDocument() },
            disposal?.id?.let { getUnpaidWorkTime(it) },
            licenceConditions = disposal?.let {
                licenceConditionRepository.findAllByDisposalId(disposal.id).map {
                    it.toLicenceCondition()
                }
            } ?: emptyList()
        )
    }

    fun ExtraSentence.toAdditionalSentence(): AdditionalSentence =
        AdditionalSentence(length, amount, notes, type.description)

    fun Disposal.toOrder() =
        Order(description = type.description, length = length, startDate = date, endDate = expectedEndDate())

    fun RequirementDetails.toRequirement(): Requirement {
        val rar = getRar(id, code)

        val requirement = Requirement(
            code,
            expectedStartDate,
            startDate,
            expectedEndDate,
            terminationDate,
            terminationReason,
            populateRequirementDescription(description, codeDescription, rar),
            length,
            lengthUnitValue,
            notes,
            rar
        )

        return requirement
    }

    fun populateRequirementDescription(description: String, codeDescription: String?, rar: Rar?): String {
        rar?.let { return "" + it.totalDays + " days RAR, " + it.completed + " completed" }

        if (codeDescription != null) {
            return "$description - $codeDescription"
        }

        return description
    }

    private fun getRar(requirementId: Long, requirementType: String): Rar? {
        if (requirementType.equals("F", true)) {
            val rarDays = requirementRepository.getRarDaysByRequirementId(requirementId)
            val scheduledDays = rarDays.find { it.type == "SCHEDULED" }?.days ?: 0
            val completedDays = rarDays.find { it.type == "COMPLETED" }?.days ?: 0
            return Rar(completed = completedDays, scheduled = scheduledDays)
        }

        return null
    }

    fun getUnpaidWorkTime(disposalId: Long): String? {
        val totalHoursOrdered = requirementRepository.sumTotalUnpaidWorkHoursByDisposal(disposalId)

        if (totalHoursOrdered == 0L) {
            return null
        }

        val durationInMinutes: Long = upwAppointmentRepository.calculateUnpaidTimeWorked(disposalId)

        return getUnpaidWorkTime(totalHoursOrdered, durationInMinutes)
    }

    fun getUnpaidWorkTime(hoursOrdered: Long, minutesCredited: Long): String {
        val totalMessage = hoursOrdered
            .let { " (of $hoursOrdered hour${if (hoursOrdered != 1L) "s" else ""})" }

        val creditedMessage = Duration.ofMinutes(minutesCredited).toKotlinDuration()
            .toComponents { hours, minutes, _, _ ->
                when {
                    hours == 0L -> "$minutes minute${if (minutes != 1) "s" else ""} completed"
                    minutes == 0 -> "$hours hour${if (hours != 1L) "s" else ""} completed"
                    else -> "$hours hour${if (hours != 1L) "s" else ""} $minutes minute${if (minutes != 1) "s" else ""} completed"
                }
            }
        return "$creditedMessage$totalMessage"
    }

    fun CourtDocumentDetails.toCourtDocument(): CourtDocument = CourtDocument(id, lastSaved, documentName)

    fun getMostRecentTerminatedDateFromInactiveEvents(events: List<Event>): LocalDate? {
        if (events.isNotEmpty()) {
            return events.sortedByDescending { it.disposal?.terminationDate }[0].disposal?.terminationDate
        }
        return null
    }
}