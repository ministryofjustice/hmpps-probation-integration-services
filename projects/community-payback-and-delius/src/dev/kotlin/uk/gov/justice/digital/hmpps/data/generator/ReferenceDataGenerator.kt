package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Dataset
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

    fun generateReferenceData(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        datasetId: Long,
        selectable: Boolean = true
    ) = ReferenceData(id, code, description, datasetId, selectable)
}

object DatasetGenerator {
    val UPW_PROJECT_TYPE_DATASET = generateDataset(code = Dataset.UPW_PROJECT_TYPE)

    fun generateDataset(
        id: Long = IdGenerator.getAndIncrement(),
        code: String
    ) = Dataset(id, code)
}