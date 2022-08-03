package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ReferenceData

object ReferenceDataGenerator {
    val INITIAL_OM_ALLOCATION = generate(DatasetGenerator.OM_ALLOCATION_REASON, "IN1")
    val INITIAL_ORDER_ALLOCATION = generate(DatasetGenerator.ORDER_ALLOCATION_REASON, "INT")
    val INITIAL_RM_ALLOCATION = generate(DatasetGenerator.RM_ALLOCATION_REASON, "IN1")
    val PENDING_TRANSFER = generate(DatasetGenerator.TRANSFER_STATUS, "PN")

    fun generate(
        dataset: Dataset,
        code: String,
        description: String = code,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(
        id,
        code,
        description,
        dataset
    )
}
