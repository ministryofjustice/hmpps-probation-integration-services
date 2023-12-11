package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.controller.entity.CaseEntity

object CaseEntityGenerator {

    val DEFAULT = generate("F001022")

    fun generate(
        crn: String,
        id: Long = IdGenerator.getAndIncrement()
    ): CaseEntity {
        return CaseEntity(
            id = id,
            crn = crn,
            gender = ReferenceDataGenerator.GENDER_MALE,
            dynamicRsrScore = 10.1,
            tier = ReferenceDataGenerator.TIER_ONE
        )
    }
}
