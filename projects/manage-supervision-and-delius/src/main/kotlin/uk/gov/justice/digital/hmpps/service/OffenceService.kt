package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.offence.Offence
import uk.gov.justice.digital.hmpps.api.model.offence.OffenceDetails
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getPerson
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
            person.name(),
            event?.toOffence(),
            event?.notes,
            event?.additionalOffences?.map { it.toOffence() })
    }

    private fun Event.toOffence(): Offence =
        Offence(mainOffence?.offence?.description, mainOffence?.offence?.category, mainOffence?.date)

    private fun AdditionalOffence.toOffence(): Offence = Offence(offence.description, offence.category, date)
}