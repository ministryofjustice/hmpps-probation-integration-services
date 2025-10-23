package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.sentence.Event
import java.time.LocalDate

object EventGenerator {
    fun generate(person: Person, number: Int) = Event(
        id = id(),
        number = "$number",
        ftcCount = 0,
        breachEnd = LocalDate.now().minusDays(30),
        person = person,
        disposal = null,
        twoThirdsContacts = listOf(),
    )
}
