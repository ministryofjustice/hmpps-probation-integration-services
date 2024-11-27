package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData

object PersonGenerator {
    val NO_ROSH = generate("A000001")
    val LOW_ROSH = generate("A000002")
    val MEDIUM_ROSH = generate("A000003")
    val HIGH_ROSH = generate("A000004")
    val VERY_HIGH_ROSH = generate("A000005")
    val PERSON_NO_EVENT = generate("A000006")
    val PERSON_SOFT_DELETED_EVENT = generate("A000007")
    val PRISON_ASSESSMENT = generate("A000008")

    val NO_EXISTING_RISKS = generate("A000009")
    val EXISTING_RISKS = generate("A000010")
    val FEATURE_FLAG = generate("A000011")

    fun generate(
        crn: String,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(crn, softDeleted, id)

    fun generateManager(
        person: Person,
        probationAreaId: Long = ProviderGenerator.DEFAULT_PROVIDER_ID,
        teamId: Long = ProviderGenerator.DEFAULT_TEAM_ID,
        staffId: Long = ProviderGenerator.DEFAULT_STAFF_ID,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(person, probationAreaId, teamId, staffId, active, softDeleted, id)

    fun generateEvent(
        person: Person,
        number: String = "1",
        disposal: Disposal? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(number, person.id, disposal, active, softDeleted, id)

    fun generateDisposal(
        event: Event,
        type: DisposalType = ReferenceDataGenerator.DISPOSAL_TYPE,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(event, type, active, softDeleted, id)

    fun generateRequirement(
        person: Person,
        mainCategory: RequirementMainCategory = ReferenceDataGenerator.REQ_MAIN_CATS.first(),
        additionalMainCategory: RequirementAdditionalMainCategory? = null,
        subCategory: ReferenceData? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Requirement(person, mainCategory, additionalMainCategory, subCategory, active, softDeleted, id)
}