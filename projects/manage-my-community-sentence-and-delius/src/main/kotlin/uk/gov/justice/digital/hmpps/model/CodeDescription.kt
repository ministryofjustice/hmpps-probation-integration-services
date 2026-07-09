package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.licencecondition.LicenceConditionMainCategory
import uk.gov.justice.digital.hmpps.entity.sentence.requirement.RequirementMainCategory

data class CodeDescription(
    val code: String,
    val description: String,
) {
    companion object {
        fun RequirementMainCategory.toModel() = CodeDescription(code, description)
        fun LicenceConditionMainCategory.toModel() = CodeDescription(code, description)
        fun ReferenceData.toModel() = CodeDescription(code, description)
    }
}