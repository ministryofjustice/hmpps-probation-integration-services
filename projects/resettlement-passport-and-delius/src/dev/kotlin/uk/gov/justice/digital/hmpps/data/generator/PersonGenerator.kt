package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person

object PersonGenerator {
    val DEFAULT = generate("X123123")

    fun generate(crn: String, noms: String? = null, softDeleted: Boolean = false, id: Long = IdGenerator.getAndIncrement()) =
        Person(
            id,
            crn,
            noms,
            softDeleted
        )
}
