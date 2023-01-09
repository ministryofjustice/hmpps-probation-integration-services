package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object EventGenerator {
    val DEFAULT = generate(eventNumber = "1")
    val NEW = generate(eventNumber = "2")
    val HISTORIC = generate(eventNumber = "3")
    val INACTIVE = generate(eventNumber = "99", active = false)

    fun generate(
        person: Person = PersonGenerator.DEFAULT,
        eventNumber: String = "1",
        id: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true,
        softDeleted: Boolean = false,
    ) = Event(id, eventNumber, person, active, softDeleted)
}
