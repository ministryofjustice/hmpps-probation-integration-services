package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Event

object EventGenerator {
    val DEFAULT_EVENT = generateEvent(PersonGenerator.DEFAULT_PERSON, number = "1")
    val NO_DOCUMENT_EVENT = generateEvent(PersonGenerator.DEFAULT_PERSON, number = "2")
    val TERMINATED_EVENT = generateEvent(PersonGenerator.DEFAULT_PERSON, number = "3", active = false)

    fun generateEvent(
        person: uk.gov.justice.digital.hmpps.integrations.delius.Person,
        number: String,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Event(person = person, active = active, softDeleted = softDeleted, id = id, number = number)
}

