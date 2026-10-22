package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import java.time.LocalDate

object PersonGenerator {
    fun generate(
        crn: String,
        id: Long = IdGenerator.getAndIncrement()
    ): Person {
        val person = Person(
            id = id,
            crn = crn,
            forename = "Test",
            surname = "Person",
            dateOfBirth = LocalDate.now().minusYears(18)
        )
        person.managers.add(PersonManagerGenerator.generate(person, staff = StaffGenerator.DEFAULT))
        return person
    }
}

object PersonManagerGenerator {
    fun generate(
        person: Person,
        staff: Staff = StaffGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(
        id = id,
        person = person,
        staff = staff,
        probationArea = ProbationAreaGenerator.DEFAULT
    )
}

object ProbationAreaGenerator {
    val DEFAULT = ProbationArea(IdGenerator.getAndIncrement(), "ZZZ")
}
