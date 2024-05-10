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
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.Duration
import java.time.LocalDate
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
    private val upwAppointmentRepository: UpwAppointmentRepository
) {
    fun getEvents(crn: String): SentenceOverview {
        val person = personRepository.getPerson(crn)
        val (activeEvents, inactiveEvents) = eventRepository.findSentencesByPersonId(person.id).partition { it.active }

        return SentenceOverview(
            personSummary = person.toSummary(),
            sentences = activeEvents.map {
                val courtAppearance = courtAppearanceRepository.getFirstCourtAppearanceByEventIdOrderByDate(it.id)
                val additionalSentences = additionalSentenceRepository.getAllByEventId(it.id)
                it.toSentence(courtAppearance, additionalSentences, crn)
            },
            ProbationHistory(
                inactiveEvents.count(),
                getMostRecentTerminatedDateFromInactiveEvents(inactiveEvents),
                inactiveEvents.count { it.inBreach },
                offenderManagerRepository.countOffenderManagersByPerson(person)
            )
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
            requirements = requirementRepository.getRequirements(id, eventNumber)
                .map { it.toRequirement(id, eventNumber) },
            courtDocuments = documentRepository.getCourtDocuments(id, eventNumber).map { it.toCourtDocument() }
        )

    fun ExtraSentence.toAdditionalSentence(): AdditionalSentence =
        AdditionalSentence(length, amount, notes, type.description)

    fun Person.toName() =
        Name(forename, secondName, surname)

    fun Disposal.toOrder() =
        Order(description = type.description, length = length, startDate = date, endDate = expectedEndDate())

    fun RequirementDetails.toRequirement(eventId: Long, eventNumber: String): Requirement {
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
            getUnpaidWorkTime(code, eventId, eventNumber),
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

    fun getUnpaidWorkTime(requirementType: String, eventId: Long, eventNumber: String): String? {
        if (requirementType.equals("W", true)) {
            val durationInMinutes: Long = upwAppointmentRepository.calculateUnpaidTimeWorked(eventId)
            durationInMinutes.let {
                val duration = Duration.ofMinutes(it)
                val completed = "completed"
                val hour = "hour"
                val hours = "hours"
                val minuteCompleted = "minute $completed"
                val minutesCompleted = "minutes $completed"

                return when (duration.toHoursPart()) {
                    0 -> {
                        when (duration.toMinutesPart()) {
                            1 -> {
                                "${duration.toMinutesPart()} " + minuteCompleted
                            }

                            else -> "${duration.toMinutesPart()} " + minutesCompleted
                        }
                    }

                    1 -> {
                        when (duration.toMinutesPart()) {
                            0 -> {
                                "${duration.toHoursPart()} $hour $completed"
                            }

                            else -> "${duration.toHoursPart()} $hours and ${duration.toMinutesPart()} $minuteCompleted"
                        }
                    }

                    else -> {
                        when (duration.toMinutesPart()) {
                            0 -> {
                                "${duration.toHoursPart()} $hours $completed"
                            }

                            1 -> {
                                "${duration.toHoursPart()} $hours and ${duration.toMinutesPart()} $minuteCompleted"
                            }

                            else -> {
                                "${duration.toHoursPart()} $hours and ${duration.toMinutesPart()} $minutesCompleted"
                            }
                        }
                    }
                }
            }
        }

        return null
    }

    fun CourtDocumentDetails.toCourtDocument(): CourtDocument = CourtDocument(id, lastSaved, documentName)

    fun getMostRecentTerminatedDateFromInactiveEvents(events: List<Event>): LocalDate? {
        if (events.isNotEmpty()) {
            return events.sortedByDescending { it.disposal?.terminationDate }[0].disposal?.terminationDate
        }
        return null
    }
}