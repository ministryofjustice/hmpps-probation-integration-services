package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.entity.EnforcementAction
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData

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
    val EXCELLENT_BEHAVIOUR= generateReferenceData(
        code = "EX",
        description = "Excellent",
        datasetId = DatasetGenerator.UPW_BEHAVIOUR_DATASET.id,
        selectable = true
    )
    val UNSATISFACTORY_BEHAVIOUR = generateReferenceData(
        code = "US",
        description = "Unsatisfactory",
        datasetId = DatasetGenerator.UPW_BEHAVIOUR_DATASET.id,
        selectable = true
    )
    val DEFAULT_ENFORCEMENT_ACTION = generateEnforcementAction(
        code = "DEF",
        description = "Default Enforcement",
        responseByPeriod = 7L,
        outstandingContactAction = true
    )
    val ATTENDED_COMPLIED_CONTACT_OUTCOME = generateContactOutcome(
        code = "A",
        description = "Attended - Complied"
    )
    val FAILED_TO_ATTEND_CONTACT_OUTCOME = generateContactOutcome(
        code = "F",
        description = "Failed to Attend"
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
        outstandingContactAction: Boolean
    ) = EnforcementAction(id, code, description, responseByPeriod, outstandingContactAction)

    fun generateContactOutcome(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
    ) = ContactOutcome(id, code, description)
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