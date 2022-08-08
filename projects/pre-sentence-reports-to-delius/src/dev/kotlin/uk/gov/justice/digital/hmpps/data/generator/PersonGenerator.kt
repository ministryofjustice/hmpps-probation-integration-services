package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object PersonGenerator {
    val DEFAULT = generate("X123456")

    fun generate(crn: String, id: Long = IdGenerator.getAndIncrement()) = Person(id, crn)
}
