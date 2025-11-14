package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.overview.Order
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.api.model.sentence.AdditionalSentence
import uk.gov.justice.digital.hmpps.api.model.sentence.Offence
import uk.gov.justice.digital.hmpps.api.model.sentence.Requirement
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.breachCountForEvents
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.CourtDocumentDetails
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.LengthUnit
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.Duration
import java.time.LocalDate
import kotlin.time.toKotlinDuration
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Requirement as RequirementEntity
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
    private val licenceConditionRepository: LicenceConditionRepository,
    private val custodyRepository: CustodyRepository,
    private val requirementService: RequirementService,
    private val nsiRepository: NsiRepository,
) {
    fun getEvents(crn: String, eventNumber: String?, includeRarRequirements: Boolean): SentenceOverview {
        val person = personRepository.getPerson(crn)
        val activeEvents = getActiveSentences(person.id)

        return SentenceOverview(
            personSummary = person.toSummary(),
            activeEvents.map { it.toSentenceSummary() },
            sentence = activeEvents.firstOrNull {
                when (eventNumber) {
                    null -> true
                    else -> eventNumber == it.eventNumber
                }
            }?.toSentence(includeRarRequirements)
        )
    }

    fun getActiveSentences(crn: String, includeRarRequirements: Boolean): MinimalSentenceOverview {
        val person = personRepository.getPerson(crn)
        val activeEvents = getActiveSentences(person.id)

        return MinimalSentenceOverview(
            personSummary = person.toSummary(),
            activeEvents.map { it.toMinimalSentence(includeRarRequirements) }
        )
    }

    fun getProbationHistory(crn: String): History {
        val person = personRepository.getPerson(crn)
        val (activeEvents, inactiveEvents) = eventRepository.findSentencesByPersonId(person.id)
            .partition { !it.isInactiveEvent() }

        return History(
            personSummary = person.toSummary(),
            activeEvents.map { it.toSentenceSummary() },
            ProbationHistory(
                inactiveEvents.count(),
                getMostRecentTerminatedDateFromInactiveEvents(inactiveEvents),
                nsiRepository.breachCountForEvents(inactiveEvents.map { it.id }.toSet()),
                offenderManagerRepository.countOffenderManagersByPersonAndActiveIsFalse(person)
            )
        )
    }

    fun getActiveSentences(id: Long) = eventRepository.findSentencesByPersonId(id).filter {
        !it.isInactiveEvent()
    }

    fun getInactiveEvent(event: Event?) = event?.toInactiveSentence()

    fun Event.toSentenceSummary() = SentenceSummary(
        eventNumber,
        disposal?.type?.description ?: "Pre-Sentence"
    )

    fun Event.toMinimalSentence(includeRarRequirements: Boolean): MinimalSentence =
        MinimalSentence(
            id,
            eventNumber,
            disposal?.toMinimalOrder(),
            licenceConditions = disposal?.let {
                licenceConditionRepository.findAllByDisposalId(disposal.id).asMinimals()
            } ?: emptyList(),
            requirements = requirementRepository.getRequirements(id, eventNumber, includeRarRequirements).asMinimals {
                requirementService.getRar(it.disposal!!.id, it.mainCategory!!.code)
            }
        )

    fun Event.toSentence(includeRarRequirements: Boolean): Sentence {
        val courtAppearance = courtAppearanceRepository.getFirstCourtAppearanceByEventIdOrderByDate(id)
        val additionalSentences = additionalSentenceRepository.getAllByEventId(id)

        return Sentence(
            toOffenceDetails(),
            toConviction(courtAppearance, additionalSentences),
            order = disposal?.toOrder(),
            requirements = requirementRepository.getRequirements(id, eventNumber, includeRarRequirements).rSort()
                .map { it.toRequirement() },
            courtDocuments = documentRepository.getCourtDocuments(id, eventNumber).map { it.toCourtDocument() },
            unpaidWorkProgress = disposal?.id?.let { getUnpaidWorkTime(it) },
            licenceConditions = disposal?.let {
                licenceConditionRepository.findAllByDisposalId(disposal.id).lcSort().map { it.toLicenceCondition() }
            } ?: emptyList()
        )
    }

    fun Event.toInactiveSentence(): Sentence {
        val courtAppearance = courtAppearanceRepository.getFirstCourtAppearanceByEventIdOrderByDate(id)
        val additionalSentences = additionalSentenceRepository.getAllByEventId(id)

        return Sentence(
            toOffenceDetails(),
            toConviction(courtAppearance, additionalSentences),
            order = disposal?.toOrder(),
            courtDocuments = documentRepository.getCourtDocuments(id, eventNumber).map { it.toCourtDocument() },
            requirements = null,
            licenceConditions = null
        )
    }

    fun Event.toOffenceDetails() = OffenceDetails(
        eventNumber = eventNumber,
        offence = mainOffence?.let { Offence(it.offence.description, it.offenceCount) },
        dateOfOffence = mainOffence?.date,
        notes = notes,
        additionalOffences = additionalOffences.map {
            Offence(description = it.offence.description, count = it.offenceCount ?: 0)
        }
    )

    fun Event.toConviction(courtAppearance: CourtAppearance?, additionalSentences: List<ExtraSentence>) = Conviction(
        sentencingCourt = courtAppearance?.court?.name,
        responsibleCourt = court?.name,
        convictionDate = convictionDate,
        additionalSentences.map { it.toAdditionalSentence() }
    )

    fun ExtraSentence.toAdditionalSentence(): AdditionalSentence =
        AdditionalSentence(length, amount, notes, type.description, sentenceNotes = notes?.let { formatNote(it, true) })

    fun Disposal.toOrder(): Order {
        val sentence = custodyRepository.findAllByDisposalId(id).firstOrNull()
        return Order(
            description = type.description,
            length = length,
            startDate = date,
            endDate = expectedEndDate(),
            releaseDate = sentence?.mostRecentRelease()?.date?.toLocalDate()
        )
    }

    fun RequirementEntity.toRequirement(): Requirement {
        val rar = requirementService.getRar(disposal!!.id, mainCategory!!.code)

        val requirement = Requirement(
            id,
            mainCategory.code,
            expectedStartDate,
            startDate,
            expectedEndDate,
            terminationDate,
            terminationDetails?.description,
            populateRequirementDescription(mainCategory.description, subCategory?.description, length, rar),
            length,
            mainCategory.unitDetails?.description,
            toRequirementNote(true),
            rar = rar,
            active = active,
        )

        return requirement
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
        val totalMessage = hoursOrdered.let { " (of ${hours(hoursOrdered)})" }

        val creditedMessage = Duration.ofMinutes(minutesCredited).toKotlinDuration()
            .toComponents { hours, minutes, _, _ ->
                when {
                    hours == 0L -> "${minutes(minutes)} completed"
                    minutes == 0 -> "${hours(hours)} completed"
                    else -> "${hours(hours)} ${minutes(minutes)} completed"
                }
            }
        return "$creditedMessage$totalMessage"
    }

    private fun minutes(minutes: Int): String = "$minutes minute${singularOrPlural(minutes.toLong())}"
    private fun hours(hours: Long): String = "$hours hour${singularOrPlural(hours)}"

    private fun singularOrPlural(num: Long): String = if (num == 1L) "" else "s"

    fun CourtDocumentDetails.toCourtDocument(): CourtDocument = CourtDocument(id, lastSaved, documentName)

    fun getMostRecentTerminatedDateFromInactiveEvents(events: List<Event>): LocalDate? {
        if (events.isNotEmpty()) {
            return events.sortedByDescending { it.disposal?.terminationDate }[0].disposal?.terminationDate
        }
        return null
    }
}

fun formatNote(notes: String?, truncateNote: Boolean): List<NoteDetail> {
    return notes?.takeIf { it.isNotEmpty() }?.let {
        val splitParam = "---------------------------------------------------------" + System.lineSeparator()
        notes.split(splitParam).asReversed().mapIndexed { index, note ->
            val matchResult = Regex(
                "^Comment added by (.+?) on (\\d{2}/\\d{2}/\\d{4}) at \\d{2}:\\d{2}"
                    + System.lineSeparator()
            ).find(note)
            val commentLine = matchResult?.value
            val commentText =
                commentLine?.let { note.removePrefix(commentLine).removeSuffix(System.lineSeparator()) } ?: note

            val userCreatedBy = matchResult?.groupValues?.get(1)
            val dateCreatedBy = matchResult?.groupValues?.get(2)
                ?.let { LocalDate.parse(it, DeliusDateFormatter) }


            NoteDetail(
                index,
                userCreatedBy,
                dateCreatedBy,
                when (truncateNote) {
                    true -> {
                        if (commentText.removeSuffix(System.lineSeparator()).chunked(1500).isNotEmpty()) {
                            commentText.removeSuffix(System.lineSeparator()).chunked(1500)[0]
                        } else {
                            ""
                        }
                    }

                    else -> commentText
                },
                when (truncateNote) {
                    true -> commentText.length > 1500
                    else -> null
                }
            )
        }.filter { it.note != "null" && it.note.isNotEmpty() }
    } ?: listOf()
}

fun Disposal.toMinimalOrder(): MinimalOrder {
    val length = length?.let {
        when (lengthUnit?.code) {
            LengthUnit.YEARS.code -> it / 12
            else -> it
        }
    }

    return MinimalOrder(type.description + (lengthUnit?.let { " (${length} ${it.description})" } ?: ""),
        date,
        expectedEndDate())
}

