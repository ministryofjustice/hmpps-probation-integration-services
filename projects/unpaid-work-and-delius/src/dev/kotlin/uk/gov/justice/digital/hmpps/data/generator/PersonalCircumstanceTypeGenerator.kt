package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.common.entity.PersonalCircumstanceType

object PersonalCircumstanceTypeGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = PersonalCircumstanceType(id, "PM", "Pregnancy")
}
