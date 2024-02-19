package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData

object ReferenceDataGenerator {
    val ADDRESS_STATUS_DS = generateDataset(Dataset.Code.ADDRESS_STATUS.value)
    val DISABILITY_TYPE_DS = generateDataset(Dataset.Code.DISABILITY.value)
    val ETHNICITY_DS = generateDataset(Dataset.Code.ETHNICITY.value)
    val GENDER_DS = generateDataset(Dataset.Code.GENDER.value)
    val LANGUAGE_DS = generateDataset(Dataset.Code.LANGUAGE.value)
    val RELIGION_DS = generateDataset(Dataset.Code.RELIGION.value)

    val ADDRESS_MAIN = generateRefData("M", datasetId = ADDRESS_STATUS_DS.id)
    val ADDRESS_OTHER = generateRefData("O", datasetId = ADDRESS_STATUS_DS.id)
    val DISABILITY1 = generateRefData("DIS1", datasetId = DISABILITY_TYPE_DS.id)
    val DISABILITY2 = generateRefData("DIS1", datasetId = DISABILITY_TYPE_DS.id)
    val DISABILITY3 = generateRefData("DIS1", datasetId = DISABILITY_TYPE_DS.id)
    val ETHNICITY = generateRefData("ETH1", datasetId = ETHNICITY_DS.id)
    val GENDER = generateRefData("GEN1", datasetId = GENDER_DS.id)
    val LANGUAGE = generateRefData("LANG1", datasetId = LANGUAGE_DS.id)
    val RELIGION = generateRefData("REL1", datasetId = RELIGION_DS.id)

    fun generateDataset(code: String) = Dataset(code, IdGenerator.getAndIncrement())
    fun generateRefData(
        code: String,
        description: String = "Description of $code",
        datasetId: Long,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(code, description, id, datasetId)

    fun allDatasets() = listOf(ADDRESS_STATUS_DS, DISABILITY_TYPE_DS, ETHNICITY_DS, GENDER_DS, LANGUAGE_DS, RELIGION_DS)
    fun allReferenceData() = listOf(
        ADDRESS_MAIN,
        ADDRESS_OTHER,
        DISABILITY1,
        DISABILITY2,
        DISABILITY3,
        ETHNICITY,
        GENDER,
        LANGUAGE,
        RELIGION
    )
}
