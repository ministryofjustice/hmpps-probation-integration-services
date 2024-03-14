package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*

object ReferenceDataGenerator {
    val CUSTODY_STATUS = "THROUGHCARE STATUS"
    val KEY_DATE_TYPE = "THROUGHCARE DATE TYPE"

    val DATASET_LC_SUB_CAT = generateDataset(Dataset.SUB_CATEGORY_CODE)
    val DATASET_LM_ALLOCATION_REASON = generateDataset(Dataset.LM_ALLOCATION_REASON)
    val DATASET_CUSTODY_STATUS = generateDataset(CUSTODY_STATUS)
    val DATASET_KEY_DATE_TYPE = generateDataset(KEY_DATE_TYPE)
    val LC_STANDARD_CATEGORY = generateLcCategory(LicenceConditionCategory.STANDARD_CATEGORY_CODE)
    val LC_STANDARD_SUB_CATEGORY =
        generateReferenceData(ReferenceData.STANDARD_SUB_CATEGORY_CODE, dataset = DATASET_LC_SUB_CAT)
    val LC_BESPOKE_CATEGORY = generateLcCategory(LicenceConditionCategory.BESPOKE_CATEGORY_CODE)
    val LC_BESPOKE_SUB_CATEGORY =
        generateReferenceData(ReferenceData.BESPOKE_SUB_CATEGORY_CODE, dataset = DATASET_LC_SUB_CAT)
    val DEFAULT_TRANSFER_REASON = generateTransferReason(TransferReason.DEFAULT_CODE)
    val INITIAL_ALLOCATION_REASON =
        generateReferenceData(ReferenceData.INITIAL_ALLOCATION_CODE, dataset = DATASET_LM_ALLOCATION_REASON)
    val PSS_COMMENCED_STATUS = generateReferenceData("P", dataset = DATASET_CUSTODY_STATUS)
    val RELEASED_STATUS = generateReferenceData("B", dataset = DATASET_CUSTODY_STATUS)
    val SENTENCE_EXPIRY_DATE_TYPE =
        generateReferenceData(ReferenceData.SENTENCE_EXPIRY_CODE, dataset = DATASET_KEY_DATE_TYPE)
    val CVL_MAPPINGS = listOf(
        generateCvlMapping(
            "AdditionalLcOne",
            generateLcCategory("ADD1"),
            generateLcSubCategory("ADD1S")
        ),
        generateCvlMapping(
            "AdditionalLcTwo",
            generateLcCategory("ADD2"),
            generateLcSubCategory("ADD2S")
        ),
        generateCvlMapping(
            "EmLcOne",
            generateLcCategory("EM1"),
            generateLcSubCategory("EM1S"),
            "curfew"
        ),
        generateCvlMapping(
            "VictimLcOne",
            generateLcCategory("VIC1"),
            generateLcSubCategory("NCL3"),
            populateNotes = false
        )
    )
    val CONTACT_TYPE_LPOP = generateContactType(ContactType.LPOP)

    fun generateLcCategory(code: String, id: Long = IdGenerator.getAndIncrement()) = LicenceConditionCategory(code, id)
    fun generateDataset(code: String, id: Long = IdGenerator.getAndIncrement()) = Dataset(code, id)
    fun generateReferenceData(
        code: String,
        description: String = "Description of $code",
        dataset: Dataset,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(code, description, dataset.id, id)

    fun generateLcSubCategory(code: String, description: String = "LC SubCategory $code") =
        generateReferenceData(code, description, DATASET_LC_SUB_CAT)

    fun generateCvlMapping(
        cvlCode: String,
        mainCategory: LicenceConditionCategory,
        subCategory: ReferenceData,
        cvlModifier: String? = null,
        populateNotes: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = CvlMapping(cvlCode, mainCategory, subCategory, cvlModifier, populateNotes, id)

    fun generateContactType(code: String, id: Long = IdGenerator.getAndIncrement()) = ContactType(code, id)

    fun generateTransferReason(code: String, id: Long = IdGenerator.getAndIncrement()) = TransferReason(code, id)
}
