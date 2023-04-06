package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataSet

object ReferenceDataGenerator {

    val GENDER_MALE = generate("MALE", ReferenceDataSetGenerator.GENDER)
    val TIER_ONE = generate("T1", ReferenceDataSetGenerator.TIER)

    fun generate(
        code: String,
        dataset: ReferenceDataSet,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(id, code, "description of $code", dataset)
}

object ReferenceDataSetGenerator {
    val TIER = generate("TIER")
    val TIER_CHANGE_REASON = generate("TIER CHANGE REASON")
    val GENDER = generate("GENDER")

    fun generate(name: String, id: Long = IdGenerator.getAndIncrement()) = ReferenceDataSet(id, name)
}
