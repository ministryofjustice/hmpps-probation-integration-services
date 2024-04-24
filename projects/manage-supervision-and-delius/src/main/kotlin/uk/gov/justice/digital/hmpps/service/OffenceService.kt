package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.offence.Offence
import uk.gov.justice.digital.hmpps.api.model.offence.Offences
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository

@Service
class OffenceService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventSentenceRepository
) {

    fun getOffencesForPerson(crn: String, eventNumber: String): Offences {
        val person = personRepository.getPerson(crn)
        val event = eventRepository.getEventByPersonIdAndEventNumberAndActiveIsTrue(person.id, eventNumber)

        return Offences(
            person.toName(),
            event?.toOffence(),
            event?.additionalOffences?.map { it.toOffence() } ?: emptyList())
    }

    fun Person.toName() =
        Name(forename, secondName, surname)

    private fun Event.toOffence(): Offence? = mainOffence?.let {
        Offence(
            it.offence.description,
            it.offence.category,
            it.offence.code.trim(),
            it.date,
            it.offenceCount,
            notes
        )
    }

    private fun AdditionalOffence.toOffence(): Offence =
        Offence(offence.description, offence.category, offence.code.trim(), date, offenceCount, null)
}