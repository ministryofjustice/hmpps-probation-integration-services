package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.common.entity.Dataset
import uk.gov.justice.digital.hmpps.controller.common.entity.ReferenceData

object ReferenceDataGenerator {
    val GENDER_MALE = generate(DatasetGenerator.GENDER, "M", "Male")
    val ETHNICITY_INDIAN = generate(DatasetGenerator.ETHNICITY, "A1", "Asian or Asian British: Indian")
    val DISABILITY_HEARING = generate(DatasetGenerator.DISABILITY, "HD", "Hearing Difficulties")
    val LANGUAGE_ENGLISH = generate(DatasetGenerator.LANGUAGE, "001", "English")
    fun generate(
        dataset: Dataset,
        code: String,
        description: String = code,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(
        id,
        code,
        description,
        dataset
    )
}
