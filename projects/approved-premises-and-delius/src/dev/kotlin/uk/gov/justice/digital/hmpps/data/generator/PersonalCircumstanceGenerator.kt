package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.PersonalCircumstance
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.PersonalCircumstanceSubType
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.PersonalCircumstanceType

object PersonalCircumstanceGenerator {
    val PC_TYPES = listOf(
        generateType(PersonalCircumstanceType.Code.CARE_LEAVER.value),
        generateType(PersonalCircumstanceType.Code.VETERAN.value)
    )
    val PC_SUB_TYPES = listOf(
        generateSubType(PersonalCircumstanceType.Code.CARE_LEAVER.value + "SUB"),
        generateSubType(PersonalCircumstanceType.Code.VETERAN.value + "SUB")
    )

    fun generate(
        personId: Long,
        type: PersonalCircumstanceType,
        subType: PersonalCircumstanceSubType,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonalCircumstance(id, personId, type, subType)

    fun generateType(
        code: String,

        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonalCircumstanceType(id, code, description)

    fun generateSubType(
        code: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonalCircumstanceSubType(id, code)
}