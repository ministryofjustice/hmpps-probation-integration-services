package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DatasetCode

object DatasetGenerator {
    val GENDER = generate(DatasetCode.GENDER)
    val TIER = generate(DatasetCode.TIER)
    val TIER_CHANGE_REASON = generate(DatasetCode.TIER_CHANGE_REASON)

    fun generate(code: DatasetCode, id: Long = IdGenerator.getAndIncrement()) = Dataset(id, code)
}
