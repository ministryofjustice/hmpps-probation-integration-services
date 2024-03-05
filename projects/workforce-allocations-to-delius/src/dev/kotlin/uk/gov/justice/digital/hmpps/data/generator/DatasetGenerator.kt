package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.DatasetCode

object DatasetGenerator {
    val TRANSFER_STATUS = generate(DatasetCode.TRANSFER_STATUS)
    val OM_ALLOCATION_REASON = generate(DatasetCode.OM_ALLOCATION_REASON)
    val ORDER_ALLOCATION_REASON = generate(DatasetCode.ORDER_ALLOCATION_REASON)
    val RM_ALLOCATION_REASON = generate(DatasetCode.RM_ALLOCATION_REASON)
    val OFFICER_GRADE = generate(DatasetCode.OFFICER_GRADE)
    val CUSTODY_STATUS = generate(DatasetCode.CUSTODY_STATUS)
    val THROUGHCARE_DATE_TYPE = generate(DatasetCode.THROUGHCARE_DATE_TYPE)
    val IREPORTTYPE = generate(DatasetCode.INSTITUTIONAL_REPORT_TYPE)
    val UNITS = generate(DatasetCode.UNITS)
    val GENDER = generate(DatasetCode.GENDER)
    val REQUIREMENT_SUB_CATEGORY = generate(DatasetCode.REQUIREMENT_SUB_CATEGORY)
    val ADDRESS_TYPE = generate(DatasetCode.ADDRESS_TYPE)
    val ADDRESS_STATUS = generate(DatasetCode.ADDRESS_STATUS)
    val COURT_APPEARANCE_TYPE = generate(DatasetCode.COURT_APPEARANCE_TYPE)
    val REGISTER_TYPE_FLAG = generate(DatasetCode.REGISTER_TYPE_FLAG)

    fun generate(code: DatasetCode, id: Long = IdGenerator.getAndIncrement()) = Dataset(id, code)
}
