package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.common.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.common.entity.ReferenceData

object ReferenceDataGenerator {
    val GENDER_MALE = generate(DatasetGenerator.GENDER, "M", "Male")
    val ETHNICITY_INDIAN = generate(DatasetGenerator.ETHNICITY, "A1", "Asian or Asian British: Indian")
    val DISABILITY_HEARING = generate(DatasetGenerator.DISABILITY, "HD", "Hearing Difficulties")
    val LANGUAGE_ENGLISH = generate(DatasetGenerator.LANGUAGE, "001", "English")
    val MAPPA_LEVEL_1 = generate(DatasetGenerator.REGISTER_LEVEL, "M1", "MAPPA Level 1")
    val MAPPA_CATEGORY_2 = generate(DatasetGenerator.REGISTER_CATEGORY, "M2", "MAPPA Cat 2")
    val HEARING_PROVISION = generate(DatasetGenerator.DISABILITY_PROVISION, "H", "Hearing Aid")
    val DOCTOR_RELATIONSHIP = generate(DatasetGenerator.RELATIONSHIP, "DOC", "Doctor")

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
