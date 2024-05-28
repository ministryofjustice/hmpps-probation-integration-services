package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.conviction.Conviction
import uk.gov.justice.digital.hmpps.api.model.conviction.Offence
import uk.gov.justice.digital.hmpps.api.model.conviction.OffenceDetail
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.getByPersonAndEventNumber
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Offence as OffenceEntity

@Service
class ConvictionService(private val personRepository: PersonRepository, private val eventRepository: EventRepository) {
    fun getConvictionFor(crn: String, eventId: Long): Conviction? {
        val person = personRepository.getPerson(crn)
        val event = eventRepository.getByPersonAndEventNumber(person, eventId)

        return event.toConviction()
    }

    fun Event.toConviction(): Conviction =
        Conviction(
            id,
            eventNumber,
            active,
            inBreach,
            failureToComplyCount,
            breachEnd,
            convictionDate,
            referralDate,
            toOffences()
        )

    fun Event.toOffences(): List<Offence> {
        val mainOffence = listOf(mainOffence!!.toOffence())
        return mainOffence
    }

    fun MainOffence.toOffence(): Offence = Offence(id, true, offence.toOffenceDetail(), date)

    fun OffenceEntity.toOffenceDetail(): OffenceDetail = OffenceDetail(code, description, abbreviation)
}

