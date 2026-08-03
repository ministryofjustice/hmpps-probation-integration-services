package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person
import java.time.LocalDate

object PersonGenerator {
    val PERSON1 = Person(
        id = IdGenerator.getAndIncrement(),
        crn = "X123456",
        softDeleted = false,
    )
    val PERSON2 = Person(
        id = IdGenerator.getAndIncrement(),
        crn = "X123457",
        softDeleted = false,
    )
}