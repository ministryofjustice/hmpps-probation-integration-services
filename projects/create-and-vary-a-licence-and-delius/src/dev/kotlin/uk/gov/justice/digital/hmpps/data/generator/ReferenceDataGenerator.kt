package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*

object ReferenceDataGenerator {
    val DATASET_LC_SUB_CAT = generateDataset(Dataset.SUB_CATEGORY_CODE)
    val LC_STANDARD_CATEGORY = generateLcCategory(LicenceConditionCategory.STANDARD_CATEGORY_CODE)
    val LC_STANDARD_SUB_CATEGORY =
        generateReferenceData(ReferenceData.STANDARD_SUB_CATEGORY_CODE, dataset = DATASET_LC_SUB_CAT)
    val LC_BESPOKE_CATEGORY = generateLcCategory(LicenceConditionCategory.BESPOKE_CATEGORY_CODE)
    val LC_BESPOKE_SUB_CATEGORY =
        generateReferenceData(ReferenceData.BESPOKE_SUB_CATEGORY_CODE, dataset = DATASET_LC_SUB_CAT)
    val DEFAULT_TRANSFER_REASON = generateTransferReason(TransferReason.DEFAULT_CODE)
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
        id: Long = IdGenerator.getAndIncrement()
    ) = CvlMapping(cvlCode, mainCategory, subCategory, cvlModifier, id)

    fun generateContactType(code: String, id: Long = IdGenerator.getAndIncrement()) = ContactType(code, id)

    fun generateTransferReason(code: String, id: Long = IdGenerator.getAndIncrement()) = TransferReason(code, id)
}
