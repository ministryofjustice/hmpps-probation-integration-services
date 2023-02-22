package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person

object EventGenerator {
    val DEFAULT = generate(eventNumber = "1")

    fun generate(
        person: Person = PersonGenerator.DEFAULT,
        eventNumber: String = "1",
        id: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true,
        softDeleted: Boolean = false
    ) = Event(id, eventNumber, person, active, softDeleted)
}
