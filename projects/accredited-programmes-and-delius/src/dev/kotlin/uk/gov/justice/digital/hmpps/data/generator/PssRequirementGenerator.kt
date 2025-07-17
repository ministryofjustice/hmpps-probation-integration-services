package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.sentence.PssRequirement
import uk.gov.justice.digital.hmpps.entity.sentence.PssRequirementMainCategory
import uk.gov.justice.digital.hmpps.entity.sentence.PssRequirementSubCategory
import uk.gov.justice.digital.hmpps.entity.sentence.custody.Custody

object PssRequirementGenerator {
    fun generate(
        custody: Custody,
        mainCategory: PssRequirementMainCategory,
        subCategory: PssRequirementSubCategory? = null
    ) = PssRequirement(
        id = id(),
        mainCategory = mainCategory,
        subCategory = subCategory,
        custody = custody,
        active = true,
        softDeleted = false,
    )
}
