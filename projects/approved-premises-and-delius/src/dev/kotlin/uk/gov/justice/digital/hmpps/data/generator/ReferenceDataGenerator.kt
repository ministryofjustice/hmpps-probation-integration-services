package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.ADDRESS_TYPE
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.HOSTEL_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.DatasetCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData

object ReferenceDataGenerator {

    val AP_ADDRESS_TYPE = generate("A02", ADDRESS_TYPE.id, "Approved Premises")
    val OWNER_ADDRESS_TYPE = generate("A01A", ADDRESS_TYPE.id, "Householder")
    val MAIN_ADDRESS_STATUS = generate("M", ADDRESS_STATUS.id, "Main Address")
    val PREV_ADDRESS_STATUS = generate("P", ADDRESS_STATUS.id, "Previous Address")
    val NHC_Q001 = generate("Q001", HOSTEL_CODE.id)
    val NHC_Q002 = generate("Q002", HOSTEL_CODE.id)
    val STAFF_GRADE = generate("TEST", DatasetGenerator.STAFF_GRADE.id, "Test staff grade")

    fun generate(
        code: String,
        datasetId: Long,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(id, code, description, datasetId)

    fun all() = listOf(
        OWNER_ADDRESS_TYPE,
        AP_ADDRESS_TYPE,
        MAIN_ADDRESS_STATUS,
        PREV_ADDRESS_STATUS,
        NHC_Q001,
        NHC_Q002,
        STAFF_GRADE
    )
}

object DatasetGenerator {
    val ADDRESS_TYPE = Dataset(IdGenerator.getAndIncrement(), DatasetCode.ADDRESS_TYPE)
    val ADDRESS_STATUS = Dataset(IdGenerator.getAndIncrement(), DatasetCode.ADDRESS_STATUS)
    val HOSTEL_CODE = Dataset(IdGenerator.getAndIncrement(), DatasetCode.HOSTEL_CODE)
    val STAFF_GRADE = Dataset(IdGenerator.getAndIncrement(), DatasetCode.STAFF_GRADE)

    fun all() = listOf(ADDRESS_TYPE, ADDRESS_STATUS, HOSTEL_CODE, STAFF_GRADE)
}
