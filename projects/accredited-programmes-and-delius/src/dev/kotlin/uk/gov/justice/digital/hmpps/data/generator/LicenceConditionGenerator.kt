package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceConditionMainCategory

object LicenceConditionGenerator {
    fun generate(disposal: Disposal, mainCategory: LicenceConditionMainCategory, subCategory: ReferenceData? = null) =
        LicenceCondition(
            id = id(),
            mainCategory = mainCategory,
            subCategory = subCategory,
            disposal = disposal,
            active = true,
            softDeleted = false,
        )
}
