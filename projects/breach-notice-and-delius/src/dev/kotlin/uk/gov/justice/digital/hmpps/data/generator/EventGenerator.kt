package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateDataset
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateReferenceData
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateRequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.set

object EventGenerator {

    val DEFAULT_EVENT = generateEvent(DEFAULT_PERSON, "1")
    val DEFAULT_DISPOSAL = generateDisposal(DEFAULT_EVENT)

    val DEFAULT_RQMNT_CATEGORY = generateRequirementMainCategory("DRMC")
    val DS_REQUIREMENT_SUB_CATEOGORY = generateDataset(Dataset.REQUIREMENT_SUB_CATEGORY)
    val DEFAULT_RQMNT_SUB_CATEGORY = generateReferenceData(DS_REQUIREMENT_SUB_CATEOGORY, "DRSC")
    val DEFAULT_RQMNT = generateRequirement(DEFAULT_DISPOSAL, DEFAULT_RQMNT_CATEGORY, DEFAULT_RQMNT_SUB_CATEGORY)

    fun generateEvent(
        person: Person,
        eventNumber: String,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Event(person, eventNumber, null, active, softDeleted, id)

    fun generateDisposal(
        event: Event,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Disposal(event, active, softDeleted, id).also { event.set(Event::disposal, it) }

    fun generateRequirement(
        disposal: Disposal,
        mainCategory: RequirementMainCategory,
        subCategory: ReferenceData?,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Requirement(disposal, mainCategory, subCategory, active, softDeleted, id)
}