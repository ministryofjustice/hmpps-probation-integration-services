package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateDataset
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateDisposalType
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generatePssRequirementMainCategory
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generatePssRequirementSubCategory
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateReferenceData
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateRequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.set

object EventGenerator {
    val DEFAULT_DISPOSAL_TYPE = generateDisposalType("123")
    val DEFAULT_EVENT = generateEvent(PersonGenerator.DEFAULT_PERSON)
    val DEFAULT_DISPOSAL = generateDisposal(DEFAULT_EVENT, DEFAULT_DISPOSAL_TYPE)
    val UNSENTENCED_EVENT = generateEvent(PersonGenerator.DEFAULT_PERSON)

    val DEFAULT_RQMNT_CATEGORY = generateRequirementMainCategory("DRMC")
    val DS_REQUIREMENT_SUB_CATEOGORY = generateDataset(Dataset.REQUIREMENT_SUB_CATEGORY)

    val UNPAID_RQMT_CATEGORY = generateRequirementMainCategory("W")
    val UNPAID_RQMT_SUB_CATEOGORY_RECORDS = listOf("W01", "W02", "W03", "W04", "W05").map {
        generateReferenceData(DS_REQUIREMENT_SUB_CATEOGORY, it)
    }

    val DEFAULT_RQMNT_SUB_CATEGORY = generateReferenceData(DS_REQUIREMENT_SUB_CATEOGORY, "DRSC")
    val DEFAULT_RQMNT = generateRequirement(DEFAULT_DISPOSAL, DEFAULT_RQMNT_CATEGORY, DEFAULT_RQMNT_SUB_CATEGORY)

    val UNPAID_WORK_RQMTS = UNPAID_RQMT_SUB_CATEOGORY_RECORDS.map {
        generateRequirement(DEFAULT_DISPOSAL, UNPAID_RQMT_CATEGORY, it)
    }

    val PSS_EVENT = generateEvent(PersonGenerator.PSS_PERSON)
    val PSS_DISPOSAL = generateDisposal(PSS_EVENT, DEFAULT_DISPOSAL_TYPE)
    val PSS_CUSTODY = generateCustody(PSS_DISPOSAL)
    val DEFAULT_PSS_CATEGORY = generatePssRequirementMainCategory("PSS1")
    val DEFAULT_PSS_SUB_CATEGORY = generatePssRequirementSubCategory("PSS1")
    val PSS_REQUIREMENT = generatePssRequirement(PSS_CUSTODY, DEFAULT_PSS_CATEGORY, DEFAULT_PSS_SUB_CATEGORY)

    fun generateEvent(
        person: Person,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Event(person, null, active, softDeleted, id)

    fun generateDisposal(
        event: Event,
        disposalType: DisposalType,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Disposal(event, disposalType, active, softDeleted, id).also { event.set(Event::disposal, it) }

    fun generateCustody(
        disposal: Disposal,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Custody(disposal, softDeleted, id)

    fun generateRequirement(
        disposal: Disposal,
        mainCategory: RequirementMainCategory,
        subCategory: ReferenceData?,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Requirement(disposal, mainCategory, subCategory, active, softDeleted, id)

    fun generatePssRequirement(
        custody: Custody,
        mainCategory: PssRequirementMainCategory,
        subCategory: PssRequirementSubCategory,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = PssRequirement(custody, mainCategory, subCategory, active, softDeleted, id)
}