package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonalCircumstanceType

object PersonalCircumstanceTypeGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = PersonalCircumstanceType(id, "PM", "Pregnancy")
}
