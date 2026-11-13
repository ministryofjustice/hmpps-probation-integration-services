package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integration.delius.sentence.entity.Disposal
import uk.gov.justice.digital.hmpps.integration.delius.sentence.entity.Event
import uk.gov.justice.digital.hmpps.integration.delius.sentence.entity.Requirement
import uk.gov.justice.digital.hmpps.integration.delius.sentence.entity.RequirementMainCategory

object SentenceGenerator {
    val RESTRICTIVE_MAIN_CATEGORY =
        generateRequirementMainCategory(code = "R", description = "Restrictive", restrictive = true)
    val UPW_MAIN_CATEGORY =
        generateRequirementMainCategory(
            code = RequirementMainCategory.UPW_RQMNT_MAIN_CATEGORY,
            description = "Unpaid Work",
            restrictive = false
        )
    val NON_STANDALONE_MAIN_CATEGORY =
        generateRequirementMainCategory(code = "X", description = "Non-standalone", restrictive = false)
    val RAR_REQUIREMENT_MAIN_CATEGORY =
        generateRequirementMainCategory(
            code = RequirementMainCategory.RAR_RQMNT_MAIN_CATEGORY,
            description = "Rehabilitative Activity Requirement",
            restrictive = false
        )
    val PROBATION_REQUIREMENT_MAIN_CATEGORY =
        generateRequirementMainCategory(
            code = RequirementMainCategory.PROBATION_RQMNT_MAIN_CATEGORY,
            description = "Probation Requirement",
            restrictive = false
        )

    val STANDALONE_EVENT = generateEvent(PersonGenerator.STANDALONE_ONLY_PERSON.asPerson())
    val STANDALONE_DISPOSAL = generateDisposal(STANDALONE_EVENT)
    val STANDALONE_REQUIREMENT = generateRequirement(STANDALONE_DISPOSAL, RESTRICTIVE_MAIN_CATEGORY)

    val UPW_ONLY_EVENT = generateEvent(PersonGenerator.UPW_ONLY_PERSON.asPerson())
    val UPW_ONLY_DISPOSAL = generateDisposal(UPW_ONLY_EVENT)
    val UPW_ONLY_REQUIREMENT = generateRequirement(UPW_ONLY_DISPOSAL, UPW_MAIN_CATEGORY)

    val MIXED_EVENT_STANDALONE = generateEvent(PersonGenerator.MIXED_ORDERS_PERSON.asPerson())
    val MIXED_DISPOSAL_STANDALONE = generateDisposal(MIXED_EVENT_STANDALONE)
    val MIXED_REQUIREMENT_STANDALONE = generateRequirement(MIXED_DISPOSAL_STANDALONE, UPW_MAIN_CATEGORY)

    val MIXED_EVENT_NON_STANDALONE = generateEvent(PersonGenerator.MIXED_ORDERS_PERSON.asPerson())
    val MIXED_DISPOSAL_NON_STANDALONE = generateDisposal(MIXED_EVENT_NON_STANDALONE)
    val MIXED_REQUIREMENT_NON_STANDALONE =
        generateRequirement(MIXED_DISPOSAL_NON_STANDALONE, NON_STANDALONE_MAIN_CATEGORY)

    val NO_DISPOSAL_EVENT = generateEvent(PersonGenerator.NO_DISPOSAL_PERSON.asPerson())

    val EMPTY_REQUIREMENTS_EVENT = generateEvent(PersonGenerator.EMPTY_REQUIREMENTS_PERSON.asPerson())
    val EMPTY_REQUIREMENTS_DISPOSAL = generateDisposal(EMPTY_REQUIREMENTS_EVENT)

    val RAR_REQUIREMENT_EVENT = generateEvent(PersonGenerator.RAR_REQUIREMENT_PERSON.asPerson())
    val RAR_REQUIREMENT_DISPOSAL = generateDisposal(RAR_REQUIREMENT_EVENT)
    val RAR_REQUIREMENT = generateRequirement(RAR_REQUIREMENT_DISPOSAL, RAR_REQUIREMENT_MAIN_CATEGORY)

    val PROBATION_REQUIREMENT_EVENT = generateEvent(PersonGenerator.PROBATION_REQUIREMENT_PERSON.asPerson())
    val PROBATION_REQUIREMENT_DISPOSAL = generateDisposal(PROBATION_REQUIREMENT_EVENT)
    val PROBATION_REQUIREMENT =
        generateRequirement(PROBATION_REQUIREMENT_DISPOSAL, PROBATION_REQUIREMENT_MAIN_CATEGORY)

    val RAR_AND_PROBATION_REQUIREMENTS_EVENT =
        generateEvent(PersonGenerator.RAR_AND_PROBATION_REQUIREMENTS_PERSON.asPerson())
    val RAR_AND_PROBATION_REQUIREMENTS_DISPOSAL = generateDisposal(RAR_AND_PROBATION_REQUIREMENTS_EVENT)
    val RAR_AND_PROBATION_RAR_REQUIREMENT =
        generateRequirement(RAR_AND_PROBATION_REQUIREMENTS_DISPOSAL, RAR_REQUIREMENT_MAIN_CATEGORY)
    val RAR_AND_PROBATION_PROBATION_REQUIREMENT =
        generateRequirement(RAR_AND_PROBATION_REQUIREMENTS_DISPOSAL, PROBATION_REQUIREMENT_MAIN_CATEGORY)

    fun generateDisposal(
        event: Event,
        requirements: MutableList<Requirement> = mutableListOf(),
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(event, requirements, active, softDeleted, id)

    fun generateEvent(
        person: Person,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(person, null, active, softDeleted, id)

    fun generateRequirement(
        disposal: Disposal,
        mainCategory: RequirementMainCategory?,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Requirement(id, mainCategory, disposal, active, softDeleted)

    fun generateRequirementMainCategory(
        code: String,
        description: String,
        restrictive: Boolean,
        id: Long = IdGenerator.getAndIncrement()
    ) = RequirementMainCategory(id, code, description, restrictive)
}