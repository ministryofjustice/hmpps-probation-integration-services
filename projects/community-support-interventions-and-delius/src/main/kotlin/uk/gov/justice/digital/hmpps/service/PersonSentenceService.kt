package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.controller.model.OffenceAndSentenceDetail
import uk.gov.justice.digital.hmpps.entity.EventRepository
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy

@Service
class PersonSentenceService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
) {
    fun getPersonSentenceForCrn(personIdentifier: String): List<OffenceAndSentenceDetail> {
        val personId = if (Regex("[A-Z]\\d{6}", RegexOption.IGNORE_CASE).matches(personIdentifier)) {
            personRepository.findByCrn(personIdentifier)?.id.orNotFoundBy("CRN", personIdentifier)
        } else {
            personRepository.findByNomsNumber(personIdentifier)?.id.orNotFoundBy("NOMS Number", personIdentifier)
        }
        val events = eventRepository.findByPersonIdAndDisposalsNotEmptyAndActiveTrueAndSoftDeletedFalse(personId)

        if (events.isEmpty()) return emptyList()

        return events.map { event ->
            val mainOffence =  event.mainOffences.single().orNotFoundBy("Event", event.id)
            val disposal = event.disposals.single().orNotFoundBy("Event", event.id)

            OffenceAndSentenceDetail(
                eventId = event.id,
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