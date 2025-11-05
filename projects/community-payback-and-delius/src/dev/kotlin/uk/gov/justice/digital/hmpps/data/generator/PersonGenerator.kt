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
    fun generatePerson(
        crn: String,
        forename: String,
        secondName: String? = null,
        surname: String,
        dateOfBirth: LocalDate
    ) = Person(null, crn, forename, secondName, surname, dateOfBirth)
}