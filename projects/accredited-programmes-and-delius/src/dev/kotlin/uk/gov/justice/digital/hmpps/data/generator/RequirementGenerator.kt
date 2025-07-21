package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement
import uk.gov.justice.digital.hmpps.entity.sentence.component.RequirementMainCategory

object RequirementGenerator {
    fun generate(disposal: Disposal, mainCategory: RequirementMainCategory, subCategory: ReferenceData? = null) =
        Requirement(
            id = id(),
            mainCategory = mainCategory,
            subCategory = subCategory,
            disposal = disposal,
            active = true,
            softDeleted = false,
        )
}
