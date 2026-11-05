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
    private val disposalRepository: DisposalRepository
) {
    fun getPersonSentenceForCrn(personIdentifier: String): OffenceAndSentenceDetail {
        val personId = if (Regex("[A-Z]\\d{6}", RegexOption.IGNORE_CASE).matches(personIdentifier)) {
            personRepository.findByCrn(personIdentifier)?.id.orNotFoundBy("CRN", personIdentifier)
        } else {
            personRepository.findByNomsNumber(personIdentifier)?.id.orNotFoundBy("NOMS Number", personIdentifier)
        }
        val eventId = eventRepository.findByPersonId(personId).firstOrNull()?.id.orNotFoundBy("Person",  personIdentifier)
        val offence = mainOffenceRepository.findByEventId(eventId).firstOrNull().orNotFoundBy("Event", eventId.toString())
        val disposal = disposalRepository.findByEventId(eventId).firstOrNull().orNotFoundBy("Event", eventId.toString())

        val sentenceEndDate = disposal.enteredNotionalEndDate ?: disposal.notionalEndDate.orNotFoundBy("No end Date found for disposal", disposal.id.toString())

        return OffenceAndSentenceDetail(
            offence = CodeAndDescription(offence.offence.mainCategoryCode, offence.offence.mainCategoryDescription),
            offenceSubCategory = CodeAndDescription(offence.offence.subCategoryCode, offence.offence.subCategoryDescription),
            outcome = CodeAndDescription(disposal.disposalType.code,disposal.disposalType.description),
            sentenceEndDate = sentenceEndDate
        )
    }
}