package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData

object ReferenceDataGenerator {
    val GENDER_MALE = generate(DatasetGenerator.GENDER, "M", "Male")
    val TIER_NA = generate(DatasetGenerator.TIER, "NA", "NA")
    val TIER_CHANGE_REASON_OGRS = generate(DatasetGenerator.TIER_CHANGE_REASON, "OGRS", "OGRS")
    fun generate(
        dataset: Dataset,
        code: String,
        description: String = code,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(
        id,
        dataset,
        code,
        description
    )
}
