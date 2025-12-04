package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Person
import java.time.LocalDate

object PersonContactDetailsGenerator {
    val PERSON_CONTACT_DETAILS_1 = generatePersonContactDetails("A000002")
    val PERSON_CONTACT_DETAILS_2 = generatePersonContactDetails("A000003")

    fun generatePersonContactDetails(
        crn: String,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) =
        Person(crn, softDeleted, id, LocalDate.of(1985, 10, 1), "John", "Doe", "07123456789", "john@doe.com")
}