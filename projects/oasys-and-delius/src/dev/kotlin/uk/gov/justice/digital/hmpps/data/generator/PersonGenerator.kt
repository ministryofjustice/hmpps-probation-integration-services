package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.person.entity.Person

object PersonGenerator {
    val REGISTERED_PERSON = generate("R123456")
    val RELEASED_PERSON = generate("B123456")
    val CUSTODY_PERSON = generate("C123456")

    fun generate(
        crn: String,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(crn, softDeleted, id)
}