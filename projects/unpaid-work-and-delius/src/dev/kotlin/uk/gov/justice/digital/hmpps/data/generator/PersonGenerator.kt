package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.Person

object PersonGenerator {
    val DEFAULT = generate("D001022")

    fun generate(crn: String, id: Long = IdGenerator.getAndIncrement()) = Person(id, crn, false, listOf(), listOf())
}
