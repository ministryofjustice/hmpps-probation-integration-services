package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.common.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.common.entity.DatasetCode

object DatasetGenerator {
    val GENDER = generate(DatasetCode.GENDER)
    val ETHNICITY = generate(DatasetCode.ETHNICITY)
    val DISABILITY = generate(DatasetCode.DISABILITY)
    val LANGUAGE = generate(DatasetCode.LANGUAGE)
    val REGISTER_LEVEL = generate(DatasetCode.REGISTER_LEVEL)
    val REGISTER_CATEGORY = generate(DatasetCode.REGISTER_CATEGORY)
    val DISABILITY_PROVISION = generate(DatasetCode.DISABILITY_PROVISION)
    val RELATIONSHIP = generate(DatasetCode.RELATIONSHIP)
    val ADDRESS_STATUS = generate(DatasetCode.ADDRESS_STATUS)

    fun generate(code: DatasetCode, id: Long = IdGenerator.getAndIncrement()) = Dataset(id, code)
}
