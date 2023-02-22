package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DatasetCode

object DatasetGenerator {
    val GENDER = generate(DatasetCode.GENDER)

    fun generate(code: DatasetCode, id: Long = IdGenerator.getAndIncrement()) = Dataset(id, code)
}
