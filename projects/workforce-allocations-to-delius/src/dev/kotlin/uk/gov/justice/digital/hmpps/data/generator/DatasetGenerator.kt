package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.DatasetCode

object DatasetGenerator {
    val TRANSFER_STATUS = generate(DatasetCode.TRANSFER_STATUS)
    val OM_ALLOCATION_REASON = generate(DatasetCode.OM_ALLOCATION_REASON)
    val ORDER_ALLOCATION_REASON = generate(DatasetCode.ORDER_ALLOCATION_REASON)
    val RM_ALLOCATION_REASON = generate(DatasetCode.RM_ALLOCATION_REASON)
    val OFFICER_GRADE = generate(DatasetCode.OFFICER_GRADE)
    val UNITS = generate(DatasetCode.UNITS)
    fun generate(code: DatasetCode, id: Long = IdGenerator.getAndIncrement()) = Dataset(id, code)
}
