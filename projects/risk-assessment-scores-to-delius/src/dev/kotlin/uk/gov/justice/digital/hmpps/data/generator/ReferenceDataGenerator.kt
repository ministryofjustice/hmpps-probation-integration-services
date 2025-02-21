package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData

object ReferenceDataGenerator {
    val GENDER_MALE = generate(DatasetGenerator.GENDER, "M", "Male")
    val MERGED_TO_CRN = generate(DatasetGenerator.ADDITIONAL_IDENTIFIER_TYPE, "MTCRN", "Merged to CRN")
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
