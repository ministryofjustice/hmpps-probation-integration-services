package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.common.entity.Dataset
import uk.gov.justice.digital.hmpps.controller.common.entity.DatasetCode

object DatasetGenerator {
    val GENDER = generate(DatasetCode.GENDER)
    val ETHNICITY = generate(DatasetCode.ETHNICITY)
    val DISABILITY = generate(DatasetCode.DISABILITY)
    val LANGUAGE = generate(DatasetCode.LANGUAGE)
    val REGISTER_LEVEL = generate(DatasetCode.REGISTER_LEVEL)
    val REGISTER_CATEGORY = generate(DatasetCode.REGISTER_CATEGORY)

    fun generate(code: DatasetCode, id: Long = IdGenerator.getAndIncrement()) = Dataset(id, code)
}
