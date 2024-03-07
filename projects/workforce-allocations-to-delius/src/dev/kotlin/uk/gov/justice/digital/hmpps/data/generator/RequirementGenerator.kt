package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.REQUIREMENT_SUB_CATEGORY
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewDisposal
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewRequirement
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewRequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementAdditionalMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object RequirementGenerator {
    val DEFAULT = generate()
    val NEW = generate(id = 9001)
    val HISTORIC = generate(id = 9002)

    val CASE_VIEW = forCaseView()

    fun generate(
        person: Person = PersonGenerator.DEFAULT,
        disposal: Disposal = DisposalGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true
    ) = Requirement(
        id,
        person,
        disposal,
        RequirementMainCategoryGenerator.DEFAULT,
        RequirementAdditionalMainCategoryGenerator.DEFAULT,
        ReferenceDataGenerator.REQUIREMENT_SUB_CATEGORY,
        active
    )

    fun generate(
        mainCategory: String?,
        additionalMainCategory: String?,
        subCategory: String?,
        person: Person = PersonGenerator.DEFAULT,
        disposal: Disposal = DisposalGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement()
    ) = Requirement(
        id,
        person,
        disposal,
        mainCategory?.let { RequirementMainCategoryGenerator.generate(it, "Test") },
        additionalMainCategory?.let { RequirementAdditionalMainCategoryGenerator.generate(it, "Test") },
        subCategory?.let { ReferenceDataGenerator.generate(REQUIREMENT_SUB_CATEGORY, it, "Test") }
    )

    private fun forCaseView(
        personId: Long = PersonGenerator.CASE_VIEW.id,
        disposal: CaseViewDisposal = DisposalGenerator.CASE_VIEW,
        id: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true
    ) = CaseViewRequirement(
        id,
        personId,
        disposal,
        RequirementMainCategoryGenerator.CASE_VIEW,
        ReferenceDataGenerator.REQUIREMENT_SUB_CATEGORY,
        12,
        listOf(),
        active
    )
}

object RequirementMainCategoryGenerator {
    val DEFAULT = generate("MAIN", "Rqmnt Main Category")
    val CASE_VIEW = forCaseView("CASE_VIEW", "Main Category for Case View")

    fun generate(
        code: String,
        description: String = code,
        id: Long = IdGenerator.getAndIncrement()
    ) = RequirementMainCategory(id, code, description)

    private fun forCaseView(
        code: String,
        description: String = code,
        units: ReferenceData = ReferenceDataGenerator.UNIT_MONTHS,
        id: Long = IdGenerator.getAndIncrement()
    ) = CaseViewRequirementMainCategory(id, code, description, units)
}

object RequirementAdditionalMainCategoryGenerator {
    val DEFAULT = generate("ADDN", "Rqmnt Additional Main Category")

    fun generate(
        code: String,
        description: String = code,
        id: Long = IdGenerator.getAndIncrement()
    ) = RequirementAdditionalMainCategory(id, code, description)
}
