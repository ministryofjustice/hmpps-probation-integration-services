package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.*

object ReferenceDataGenerator {
    val GROUP_PLACEMENT_PROJECT_TYPE = generateReferenceData(
        code = "G",
        description = "Group Placement",
        datasetId = DatasetGenerator.UPW_PROJECT_TYPE_DATASET.id
    )
    val INDIVIDUAL_PLACEMENT_PROJECT_TYPE = generateReferenceData(
        code = "I",
        description = "Individual Placement",
        datasetId = DatasetGenerator.UPW_PROJECT_TYPE_DATASET.id
    )
    val INACTIVE_PROJECT_TYPE = generateReferenceData(
        code = "NA",
        description = "Individual Placement",
        datasetId = DatasetGenerator.UPW_PROJECT_TYPE_DATASET.id,
        selectable = false
    )
    val EXCELLENT_WORK_QUALITY = generateReferenceData(
        code = "EX",
        description = "Excellent",
        datasetId = DatasetGenerator.UPW_WORK_QUALITY_DATASET.id,
        selectable = true
    )
    val UNSATISFACTORY_WORK_QUALITY = generateReferenceData(
        code = "US",
        description = "Unsatisfactory",
        datasetId = DatasetGenerator.UPW_WORK_QUALITY_DATASET.id,
        selectable = true
    )
    val EXCELLENT_BEHAVIOUR = generateReferenceData(
        code = "EX",
        description = "Excellent",
        datasetId = DatasetGenerator.UPW_BEHAVIOUR_DATASET.id,
        selectable = true
    )
    val UNSATISFACTORY_BEHAVIOUR = generateReferenceData(
        code = "UN",
        description = "Unsatisfactory",
        datasetId = DatasetGenerator.UPW_BEHAVIOUR_DATASET.id,
        selectable = true
    )

    val UPW_APPOINTMENT_TYPE = generateContactType(ContactType.Code.UNPAID_WORK_APPOINTMENT.value)

    val DEFAULT_ENFORCEMENT_ACTION = generateEnforcementAction(
        code = "DEF",
        description = "Default Enforcement",
        responseByPeriod = 7L,
        outstandingContactAction = true,
        contactTypeId = UPW_APPOINTMENT_TYPE.id
    )

    val ATTENDED_COMPLIED_CONTACT_OUTCOME = generateContactOutcome(
        code = "A",
        description = "Attended - Complied",
        attended = true,
        complied = true
    )
    val FAILED_TO_ATTEND_CONTACT_OUTCOME = generateContactOutcome(
        code = "F",
        description = "Failed to Attend",
        attended = false,
        complied = false
    )

    val UPW_RQMNT_MAIN_CATEGORY = generateRequirementMainCategory(
        code = "W",
        description = "Unpaid Work"
    )

    val DEFAULT_DISPOSAL_TYPE = generateDisposalType(
        code = "100",
        description = "Community Order",
        preCja2003 = false
    )

    fun generateReferenceData(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        datasetId: Long,
        selectable: Boolean = true
    ) = ReferenceData(id, code, description, datasetId, selectable)

    fun generateEnforcementAction(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        responseByPeriod: Long,
        outstandingContactAction: Boolean,
        contactTypeId: Long
    ) = EnforcementAction(id, code, description, responseByPeriod, outstandingContactAction, contactTypeId)

    fun generateContactType(
        code: String,
        id: Long = IdGenerator.getAndIncrement(),
    ) = ContactType(code, id)

    fun generateContactOutcome(
        code: String,
        description: String,
        attended: Boolean?,
        complied: Boolean?,
        id: Long = IdGenerator.getAndIncrement(),
    ) = ContactOutcome(code, description, attended, complied, id)

    fun generateRequirementMainCategory(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
    ) = RequirementMainCategory(id, code, description)

    fun generateDisposalType(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        preCja2003: Boolean = false
    ) = DisposalType(id, code, description, preCja2003)
}

object DatasetGenerator {
    val UPW_PROJECT_TYPE_DATASET = generateDataset(code = Dataset.UPW_PROJECT_TYPE)
    val UPW_WORK_QUALITY_DATASET = generateDataset(code = Dataset.UPW_WORK_QUALITY)
    val UPW_BEHAVIOUR_DATASET = generateDataset(code = Dataset.UPW_BEHAVIOUR)

    fun generateDataset(
        id: Long = IdGenerator.getAndIncrement(),
        code: String
    ) = Dataset(id, code)
}