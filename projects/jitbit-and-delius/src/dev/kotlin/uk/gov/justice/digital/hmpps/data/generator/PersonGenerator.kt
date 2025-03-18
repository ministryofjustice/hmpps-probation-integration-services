package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT = Person(
        id = IdGenerator.getAndIncrement(),
        crn = "A000001",
        forename = "First",
        secondName = "Middle",
        thirdName = "Middle 2",
        surname = "Last",
        dateOfBirth = LocalDate.of(1980, 1, 1),
    )
    val BASIC = Person(
        id = IdGenerator.getAndIncrement(),
        crn = "A000002",
        forename = "First",
        surname = "Last",
        dateOfBirth = LocalDate.of(1980, 1, 1),
    )
}
