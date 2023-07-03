package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object PersonGenerator {
    val DEFAULT = generate("D001022")

    fun generate(crn: String, id: Long = IdGenerator.getAndIncrement()) = Person(id, crn)
}
