package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.cvl.AdditionalLicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CvlMapping
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionCategory
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ReferenceData

object ReferenceDataGenerator {
    val DATASET_LC_SUB_CAT = generateDataset("LICENCE CONDITION SUB CATEGORY")
    val LC_STANDARD_CATEGORY = generateLcCategory(CvlMapping.STANDARD_CATEGORY_CODE)
    val LC_STANDARD_SUB_CATEGORY =
        generateReferenceData(CvlMapping.STANDARD_SUB_CATEGORY_CODE, dataset = DATASET_LC_SUB_CAT)
    val LC_BESPOKE_CATEGORY = generateLcCategory(CvlMapping.BESPOKE_CATEGORY_CODE)
    val LC_BESPOKE_SUB_CATEGORY =
        generateReferenceData(CvlMapping.BESPOKE_SUB_CATEGORY_CODE, dataset = DATASET_LC_SUB_CAT)
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
}
