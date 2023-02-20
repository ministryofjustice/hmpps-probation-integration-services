package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person

object PersonGenerator {
    val DEFAULT = generate("T140223")

    fun generate(crn: String, softDeleted: Boolean = false, id: Long = IdGenerator.getAndIncrement()) =
        Person(crn, softDeleted, id)
}
