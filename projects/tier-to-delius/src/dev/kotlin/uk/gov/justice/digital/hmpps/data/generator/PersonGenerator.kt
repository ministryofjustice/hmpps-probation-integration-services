package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea

object PersonGenerator {
    fun generate(
        crn: String,
        id: Long = IdGenerator.getAndIncrement()
    ): Person {
        val person = Person(id = id, crn = crn, forename = "Test", surname = "Person")
        person.managers.add(PersonManagerGenerator.generate(person))
        return person
    }
}

object PersonManagerGenerator {
    fun generate(person: Person, id: Long = IdGenerator.getAndIncrement()) = PersonManager(
        id = id,
        person = person,
        probationArea = ProbationAreaGenerator.DEFAULT
    )
}

object ProbationAreaGenerator {
    val DEFAULT = ProbationArea(IdGenerator.getAndIncrement(), "ZZZ")
}
