package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.offence.Offence
import uk.gov.justice.digital.hmpps.api.model.offence.OffenceDetails
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository

@Service
class OffenceService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventSentenceRepository
) {

    fun getOffencesForPerson(crn: String, eventNumber: String): OffenceDetails {
        val person = personRepository.getPerson(crn)
        val event = eventRepository.getEventByPersonIdAndEventNumberAndActiveIsTrue(person.id, eventNumber)

        return OffenceDetails(
            person.toName(),
            event?.toOffence(),
            event?.notes,
            event?.additionalOffences?.map { it.toOffence() } ?: emptyList())
    }

    fun Person.toName() =
        Name(forename, secondName, surname)

    private fun Event.toOffence(): Offence =
        Offence(
            mainOffence?.offence?.description,
            mainOffence?.offence?.category,
            mainOffence?.offence?.code?.trim(),
            mainOffence?.date,
            mainOffence?.offenceCount
        )

    private fun AdditionalOffence.toOffence(): Offence =
        Offence(offence.description, offence.category, offence.code.trim(), date, offenceCount)
}