package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ReferenceData

object ReferenceDataGenerator {
    val CUSTODY_STATUS = generate(DatasetGenerator.CUSTODY_STATUS, "A")
    val KEY_DATE_EXP_REL_DATE = generate(DatasetGenerator.CUSTODY_STATUS, "EXP")
    val INITIAL_OM_ALLOCATION = generate(DatasetGenerator.OM_ALLOCATION_REASON, "IN1")
    val INITIAL_ORDER_ALLOCATION = generate(DatasetGenerator.ORDER_ALLOCATION_REASON, "INT")
    val INITIAL_RM_ALLOCATION = generate(DatasetGenerator.RM_ALLOCATION_REASON, "IN1")
    val INS_RPT_PAR = generate(DatasetGenerator.IREPORTTYPE, "PAR")
    val PENDING_TRANSFER = generate(DatasetGenerator.TRANSFER_STATUS, "PN")
    val PSQ_GRADE = generate(DatasetGenerator.OFFICER_GRADE, "PSQ")
    val UNIT_MONTHS = generate(DatasetGenerator.UNITS, "M", "Months")
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
