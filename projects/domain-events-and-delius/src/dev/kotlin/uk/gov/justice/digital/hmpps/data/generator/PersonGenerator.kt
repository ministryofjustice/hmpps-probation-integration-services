package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import java.time.LocalDate

object PersonGenerator {
    val ENGAGEMENT_CREATED =
        generate("X789654", "Bernard", "Shepherd", LocalDate.of(1977, 10, 9), pnc = "1977/9999748M")

    fun generate(
        crn: String,
        forename: String,
        surname: String,
        dateOfBirth: LocalDate,
        secondName: String? = null,
        thirdName: String? = null,
        pnc: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(crn, pnc, forename, secondName, thirdName, surname, dateOfBirth, softDeleted, id)
}
