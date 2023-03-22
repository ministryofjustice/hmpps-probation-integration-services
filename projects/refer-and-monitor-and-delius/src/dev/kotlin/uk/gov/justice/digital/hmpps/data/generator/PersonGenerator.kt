package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person

object PersonGenerator {
    val DEFAULT = generate("T140223")
    val FUZZY_SEARCH = generate("F123456")
    val SETENCED_WITHOUT_NSI = generate("S123456")

    fun generate(crn: String, softDeleted: Boolean = false, id: Long = IdGenerator.getAndIncrement()) =
        Person(crn, softDeleted, id)
}
