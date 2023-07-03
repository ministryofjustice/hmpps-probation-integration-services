package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.controller.entity.Disposal
import uk.gov.justice.digital.hmpps.controller.entity.RequirementEntity
import uk.gov.justice.digital.hmpps.controller.entity.RequirementMainCategory

object RequirementGenerator {
    val DEFAULT = generate()
    fun generate(
        disposal: Disposal = DisposalGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement()
    ) = RequirementEntity(
        id,
        disposal,
        RequirementMainCategoryGenerator.DEFAULT
    )

    object RequirementMainCategoryGenerator {
        val DEFAULT = generate("MAIN", "Rqmnt Main Category")

        fun generate(
            code: String,
            description: String = code,
            id: Long = IdGenerator.getAndIncrement()
        ) = RequirementMainCategory(id, code, description, false)
    }
}
