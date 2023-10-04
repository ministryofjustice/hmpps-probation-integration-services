package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.ADDRESS_TYPE
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.ALL_DATASETS
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.ETHNICITY
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.GENDER
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.GENDER_IDENTITY
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.HOSTEL_CODE
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.NATIONALITY
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.REGISTER_CATEGORY
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.REGISTER_LEVEL
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.RELIGION
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.MoveOnCategory
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ReferralSource
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.Category
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.Level
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.DatasetCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData

object ReferenceDataGenerator {

    val AP_ADDRESS_TYPE = generate("A02", ADDRESS_TYPE.id, "Approved Premises")
    val OWNER_ADDRESS_TYPE = generate("A01A", ADDRESS_TYPE.id, "Householder")
    val MAIN_ADDRESS_STATUS = generate("M", ADDRESS_STATUS.id, "Main Address")
    val PREV_ADDRESS_STATUS = generate("P", ADDRESS_STATUS.id, "Previous Address")
    val NHC_Q001 = generate("Q001", HOSTEL_CODE.id)
    val NHC_Q002 = generate("Q002", HOSTEL_CODE.id)
    val STAFF_GRADE = generate("TEST", DatasetGenerator.STAFF_GRADE.id, "Test staff grade")

    val REFERRAL_DATE_TYPE = generate("CRC", ALL_DATASETS[DatasetCode.AP_REFERRAL_DATE_TYPE]!!.id)
    val OTHER_REFERRAL_CATEGORY = generate("O", ALL_DATASETS[DatasetCode.AP_REFERRAL_CATEGORY]!!.id)
    val ACCEPTED_DEFERRED_ADMISSION = generate("AD", ALL_DATASETS[DatasetCode.REFERRAL_DECISION]!!.id)
    val AP_REFERRAL_SOURCE = generate("AP", ALL_DATASETS[DatasetCode.SOURCE_TYPE]!!.id)
    val YN_UNKNOWN = generate("D", ALL_DATASETS[DatasetCode.YES_NO]!!.id)
    val RISK_UNKNOWN = generate("K", ALL_DATASETS[DatasetCode.RISK_OF_HARM]!!.id)
    val ORDER_EXPIRED = generate("N", ALL_DATASETS[DatasetCode.AP_DEPARTURE_REASON]!!.id)
    val NON_ARRIVAL = generate("D", ALL_DATASETS[DatasetCode.AP_NON_ARRIVAL_REASON]!!.id)

    val OTHER_REFERRAL_SOURCE = generateReferralSource("OTH")
    val MC05 = generateMoveOnCategory("MC05")
    val REGISTER_TYPES = RegisterType.Code.entries
        .map { RegisterType(it.value, "Description of ${it.value}", IdGenerator.getAndIncrement()) }
        .associateBy { it.code }

    val REFERRAL_COMPLETED = generate("APRC", ALL_DATASETS[DatasetCode.NSI_OUTCOME]!!.id)

    val ETHNICITY_WHITE = generate("W1", ETHNICITY.id, "White: British/English/Welsh/Scottish/Northern Irish")
    val GENDER_MALE = generate("M", GENDER.id, "Male")
    val GENDER_IDENTITY_PNS = generate("GIRF", GENDER_IDENTITY.id, "Prefer not to say")
    val NATIONALITY_BRITISH = generate("BRIT", NATIONALITY.id, "British")
    val RELIGION_OTHER = generate("OTH", RELIGION.id, "Other")

    val REGISTER_CATEGORIES = Category.entries.map {
        generate(it.name, REGISTER_CATEGORY.id, "MAPPA Category ${it.name}")
    }.associateBy { it.code }
    val REGISTER_LEVELS: Map<String, ReferenceData> = Level.entries.map {
        generate(it.name, REGISTER_LEVEL.id, "MAPPA Level ${it.name}")
    }.associateBy { it.code }

    fun generate(
        code: String,
        datasetId: Long,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(id, code, description, datasetId)

    fun all(): List<ReferenceData> = listOf(
        OWNER_ADDRESS_TYPE,
        AP_ADDRESS_TYPE,
        MAIN_ADDRESS_STATUS,
        PREV_ADDRESS_STATUS,
        NHC_Q001,
        NHC_Q002,
        STAFF_GRADE,
        REFERRAL_DATE_TYPE,
        OTHER_REFERRAL_CATEGORY,
        ACCEPTED_DEFERRED_ADMISSION,
        AP_REFERRAL_SOURCE,
        YN_UNKNOWN,
        RISK_UNKNOWN,
        ORDER_EXPIRED,
        NON_ARRIVAL,
        REFERRAL_COMPLETED,
        ETHNICITY_WHITE,
        GENDER_MALE,
        GENDER_IDENTITY_PNS,
        NATIONALITY_BRITISH,
        RELIGION_OTHER
    ) + REGISTER_CATEGORIES.values + REGISTER_LEVELS.values

    fun generateReferralSource(code: String, id: Long = IdGenerator.getAndIncrement()) = ReferralSource(id, code)
    fun generateMoveOnCategory(code: String, id: Long = IdGenerator.getAndIncrement()) = MoveOnCategory(id, code)
}

object DatasetGenerator {
    val ALL_DATASETS = DatasetCode.entries.map { Dataset(IdGenerator.getAndIncrement(), it) }.associateBy { it.code }
    val ADDRESS_TYPE = ALL_DATASETS[DatasetCode.ADDRESS_TYPE]!!
    val ADDRESS_STATUS = ALL_DATASETS[DatasetCode.ADDRESS_STATUS]!!
    val HOSTEL_CODE = ALL_DATASETS[DatasetCode.HOSTEL_CODE]!!
    val STAFF_GRADE = ALL_DATASETS[DatasetCode.STAFF_GRADE]!!
    val GENDER = ALL_DATASETS[DatasetCode.GENDER]!!
    val GENDER_IDENTITY = ALL_DATASETS[DatasetCode.GENDER_IDENTITY]!!
    val ETHNICITY = ALL_DATASETS[DatasetCode.ETHNICITY]!!
    val NATIONALITY = ALL_DATASETS[DatasetCode.NATIONALITY]!!
    val RELIGION = ALL_DATASETS[DatasetCode.RELIGION]!!
    val REGISTER_CATEGORY = ALL_DATASETS[DatasetCode.REGISTER_CATEGORY]!!
    val REGISTER_LEVEL = ALL_DATASETS[DatasetCode.REGISTER_LEVEL]!!
    fun all() = ALL_DATASETS.values
}
