package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT = generate(crn = "A000001", secondName = "Middle", thirdName = "Middle 2")

    val BASIC = generate(crn = "A000002")

    val EXCLUSION = generate("E123456", exclusionMessage = "There is an exclusion on this person")
    val RESTRICTION = generate("R123456", restrictionMessage = "There is a restriction on this person")
    val RESTRICTION_EXCLUSION = generate(
        "B123456",
        exclusionMessage = "You are excluded from viewing this case",
        restrictionMessage = "You are restricted from viewing this case"
    )

    fun generate(
        crn: String,
        forename: String = "First",
        surname: String = "Last",
        secondName: String? = null,
        thirdName: String? = null,
        dateOfBirth: LocalDate = LocalDate.of(1980, 1, 1),
        exclusionMessage: String? = null,
        restrictionMessage: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(
        id,
        crn,
        forename,
        secondName,
        thirdName,
        surname,
        dateOfBirth,
        exclusionMessage,
        restrictionMessage,
        softDeleted
    )
}
