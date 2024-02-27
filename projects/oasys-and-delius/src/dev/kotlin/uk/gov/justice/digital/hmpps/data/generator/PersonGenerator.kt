package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.person.Person

object PersonGenerator {
    val REGISTERED_PERSON = generate("R123456")

    fun generate(
        crn: String,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(crn, softDeleted, id)
}