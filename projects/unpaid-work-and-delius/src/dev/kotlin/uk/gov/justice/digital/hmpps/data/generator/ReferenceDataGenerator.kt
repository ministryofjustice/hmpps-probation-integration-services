package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.common.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.common.entity.ReferenceData

object ReferenceDataGenerator {
    val GENDER_MALE = generate(DatasetGenerator.GENDER, "M", "Male")
    val ETHNICITY_INDIAN = generate(DatasetGenerator.ETHNICITY, "A1", "Asian or Asian British: Indian")
    val DISABILITY_HEARING = generate(DatasetGenerator.DISABILITY, "HD", "Hearing Difficulties")
    val DISABILITY_HEARING_CONDITION = generate(DatasetGenerator.DISABILITY_CONDITION, "HD1", "Tone deaf")
    val LANGUAGE_ENGLISH = generate(DatasetGenerator.LANGUAGE, "001", "English")
    val MAPPA_LEVEL_1 = generate(DatasetGenerator.REGISTER_LEVEL, "M1", "MAPPA Level 1")
    val MAPPA_CATEGORY_2 = generate(DatasetGenerator.REGISTER_CATEGORY, "M2", "MAPPA Cat 2")
    val HEARING_PROVISION = generate(DatasetGenerator.DISABILITY_PROVISION, "H", "Hearing Aid")
    val HEARING_PROVISION_CATEGORY =
        generate(DatasetGenerator.DISABILITY_PROVISION_CATEGORY, "H1", "Hearing Aid in one ear")
    val DOCTOR_RELATIONSHIP = generate(DatasetGenerator.RELATIONSHIP, "DOC", "Doctor")
    val MAIN_ADDRESS = generate(DatasetGenerator.ADDRESS_STATUS, "M", "Main")

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
