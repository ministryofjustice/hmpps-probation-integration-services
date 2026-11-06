package uk.gov.justice.digital.hmpps.service

import org.hibernate.validator.internal.constraintvalidators.bv.money.PositiveValidatorForMonetaryAmount
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
        val eventIds = eventRepository.findByPersonId(personId).map { it.id }
        val offences = eventIds.map {
            mainOffenceRepository.findByEventId(it).firstOrNull()
        }
        val disposals = eventIds.map {
            disposalRepository.findByEventId(it).firstOrNull()
        }

        val sentenceEndDates = disposals.map { Pair(it?.event?.id, it?.enteredNotionalEndDate ?: it?.notionalEndDate) }

        val dataToReturn = eventIds.map {
            val eventId = it
            val offence = offences.firstOrNull() { it?.eventId == eventId }.orNotFoundBy("Event", eventId)
            val disposal = disposals.firstOrNull() { it?.event?.id == eventId }.orNotFoundBy("Event", eventId)
            val sentenceEndDate = sentenceEndDates.find { it.first == eventId }?.second.orNotFoundBy("Event", eventId)

            OffenceAndSentenceDetail(
                eventId = eventId,
                offence = CodeAndDescription(offence.offence.mainCategoryCode, offence.offence.mainCategoryDescription),
                offenceSubCategory = CodeAndDescription(offence.offence.subCategoryCode, offence.offence.subCategoryDescription),
                outcome = CodeAndDescription(disposal.disposalType.code, disposal.disposalType.description),
                sentenceEndDate = sentenceEndDate
            )

        }
        return dataToReturn
    }
}