package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.sentence.Event

object EventGenerator {
    fun generate(person: Person, number: Int) = Event(
        id = id(),
        number = "$number",
        person = person,
        disposal = null,
        twoThirdsContacts = listOf(),
        active = true,
        softDeleted = false
    )
}
