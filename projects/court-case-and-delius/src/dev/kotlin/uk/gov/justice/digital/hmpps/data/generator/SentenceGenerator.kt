package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.Outcome
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import java.time.ZonedDateTime

object SentenceGenerator {
    fun generateSentence(
        event: Event,
        endDate: ZonedDateTime? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(event, endDate, active, softDeleted, id)

    fun generateEvent(
        person: Person,
        inBreach: Boolean = false,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(person, inBreach, active, softDeleted, id)

    fun generateOrderManager(
        event: Event,
        staff: Staff,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = OrderManager(event, staff, active, softDeleted, id)

    fun generateCourtAppearance(
        event: Event,
        outcome: Outcome,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = CourtAppearance(event, outcome, softDeleted, id)
}
