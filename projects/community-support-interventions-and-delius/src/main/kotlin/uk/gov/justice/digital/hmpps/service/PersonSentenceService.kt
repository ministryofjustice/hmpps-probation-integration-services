package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.controller.model.OffenceAndSentenceDetail
import uk.gov.justice.digital.hmpps.entity.DisposalRepository
import uk.gov.justice.digital.hmpps.entity.EventRepository
import uk.gov.justice.digital.hmpps.entity.MainOffenceRepository
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy

@Service
class PersonSentenceService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val mainOffenceRepository: MainOffenceRepository,
    private val disposalRepository: DisposalRepository,
) {
    fun getPersonSentenceForCrn(personIdentifier: String): List<OffenceAndSentenceDetail> {
        val personId = if (Regex("[A-Z]\\d{6}", RegexOption.IGNORE_CASE).matches(personIdentifier)) {
            personRepository.findByCrn(personIdentifier)?.id.orNotFoundBy("CRN", personIdentifier)
        } else {
            personRepository.findByNomsNumber(personIdentifier)?.id.orNotFoundBy("NOMS Number", personIdentifier)
        }
        val eventIds = eventRepository.findActiveSentencedEventsByPersonId(personId).map { it.id }

        if (eventIds.isEmpty()) return emptyList()

        val offencesByEventId = mainOffenceRepository.findByEventIdIn(eventIds)
            .groupBy { it.eventId }
            .mapValues { (_, offences) -> offences.single() }

        val disposalsByEventId = disposalRepository.findByEventIdIn(eventIds)
            .groupBy { it.event.id }
            .mapValues { (_, disposals) -> disposals.single() }

        return eventIds.map { eventId ->
            val mainOffence = offencesByEventId[eventId]
                .orNotFoundBy("Event", eventId)
            val disposal = disposalsByEventId[eventId]
                .orNotFoundBy("Event", eventId)

            OffenceAndSentenceDetail(
                eventId = eventId,
                offence = CodeAndDescription(
                    mainOffence.offence.mainCategoryCode,
                    mainOffence.offence.mainCategoryDescription
                ),
                offenceSubCategory = CodeAndDescription(
                    mainOffence.offence.subCategoryCode,
                    mainOffence.offence.subCategoryDescription
                ),
                outcome = CodeAndDescription(
                    disposal.disposalType.code,
                    disposal.disposalType.description
                ),
                sentenceEndDate = disposal.enteredNotionalEndDate
                    ?: disposal.notionalEndDate
            )
        }
    }
}