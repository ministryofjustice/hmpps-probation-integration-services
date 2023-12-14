package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.common.entity.PersonalCircumstanceSubType

object PersonalCircumstanceSubTypeGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = PersonalCircumstanceSubType(
        id,
        "D07",
        "Recently given birth"
    )
}
