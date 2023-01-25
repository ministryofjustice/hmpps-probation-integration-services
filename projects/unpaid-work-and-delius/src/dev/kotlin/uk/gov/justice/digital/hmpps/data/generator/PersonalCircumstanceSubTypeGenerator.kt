package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonalCircumstanceSubType

object PersonalCircumstanceSubTypeGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = PersonalCircumstanceSubType(id, "D07", "Recently given birth")
}
