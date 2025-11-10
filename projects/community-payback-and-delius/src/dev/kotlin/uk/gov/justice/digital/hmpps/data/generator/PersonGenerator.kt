package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT_PERSON = generatePerson(
        crn = "Z000001",
        forename = "Default",
        surname = "Person",
        dateOfBirth = LocalDate.of(1990, 6, 10)
    )

    val SECOND_PERSON = generatePerson(
        crn = "Z000222",
        forename = "Second",
        surname = "Person",
        dateOfBirth = LocalDate.of(1977, 1, 25)
    )

    fun generatePerson(
        id: Long = IdGenerator.getAndIncrement(),
        crn: String,
        forename: String,
        secondName: String? = null,
        surname: String,
        dateOfBirth: LocalDate
    ) = Person(id, crn, forename, secondName, surname, dateOfBirth)
}