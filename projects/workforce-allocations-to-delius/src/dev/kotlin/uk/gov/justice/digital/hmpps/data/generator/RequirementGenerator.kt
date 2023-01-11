package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object RequirementGenerator {
    val DEFAULT = generate()
    val NEW = generate(id = 9001)
    val HISTORIC = generate(id = 9002)

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
        ReferenceDataGenerator.REQUIREMENT_SUB_CATEGORY,
        12,
        active
    )
}

object RequirementMainCategoryGenerator {
    val DEFAULT = generate("MAIN", "Rqmnt Main Category")

    fun generate(
        code: String,
        description: String = code,
        units: ReferenceData = ReferenceDataGenerator.UNIT_MONTHS,
        id: Long = IdGenerator.getAndIncrement()
    ) = RequirementMainCategory(id, code, description, units)
}
