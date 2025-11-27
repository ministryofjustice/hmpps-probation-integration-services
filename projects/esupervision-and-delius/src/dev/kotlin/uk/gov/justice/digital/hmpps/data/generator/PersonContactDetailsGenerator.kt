package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Person

object PersonContactDetailsGenerator {
    val DEFAULT_PERSON_CONTACT_DETAILS = generatePersonContactDetails("A000002")

    fun generatePersonContactDetails(
        crn: String,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) =
        Person(crn, softDeleted, id, "John", "Doe", "07123456789", "john@doe.com")
}