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
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ApprovedPremisesCategoryCode
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

    val NHC_Q005 = generate("Q005", HOSTEL_CODE.id)
    val NHC_Q049 = generate("Q049", HOSTEL_CODE.id)
    val NHC_Q095 = generate("Q095", HOSTEL_CODE.id)
    val NHC_Q701 = generate("Q701", HOSTEL_CODE.id)
    val NHC_Q702 = generate("Q702", HOSTEL_CODE.id)
    val NHC_Q703 = generate("Q703", HOSTEL_CODE.id)
    val NHC_Q704 = generate("Q704", HOSTEL_CODE.id)
    val NHC_Q705 = generate("Q705", HOSTEL_CODE.id)
    val NHC_Q706 = generate("Q706", HOSTEL_CODE.id)
    val NHC_Q707 = generate("Q707", HOSTEL_CODE.id)
    val NHC_Q708 = generate("Q708", HOSTEL_CODE.id)
    val NHC_Q709 = generate("Q709", HOSTEL_CODE.id)
    val NHC_Q710 = generate("Q710", HOSTEL_CODE.id)
    val NHC_Q711 = generate("Q711", HOSTEL_CODE.id)
    val NHC_Q712 = generate("Q712", HOSTEL_CODE.id)
    val NHC_Q713 = generate("Q713", HOSTEL_CODE.id)
    val NHC_Q714 = generate("Q714", HOSTEL_CODE.id)
    val NHC_Q715 = generate("Q715", HOSTEL_CODE.id)
    val NHC_Q716 = generate("Q716", HOSTEL_CODE.id)

    val STAFF_GRADE = generate("TEST", DatasetGenerator.STAFF_GRADE.id, "Test staff grade")

    val REFERRAL_DATE_TYPE = generate("CRC", ALL_DATASETS[DatasetCode.AP_REFERRAL_DATE_TYPE]!!.id)
    val REFERRAL_CATEGORIES =
        ApprovedPremisesCategoryCode.entries.map {
            generate(
                it.value,
                ALL_DATASETS[DatasetCode.AP_REFERRAL_CATEGORY]!!.id
            )
        }.associateBy { it.code }
    val REFERRAL_GROUP = generate("NE", ALL_DATASETS[DatasetCode.AP_REFERRAL_GROUPING]!!.id, "North East")
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
    val GENDER_FEMALE = generate("F", GENDER.id, "Female")
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

    val NON_MAPPA_CATEGORY = generate("RC07", REGISTER_CATEGORY.id, "Other category")

    fun generate(
        code: String,
        datasetId: Long,
        description: String = "Description of $code",
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(id, code, description, datasetId, selectable)

    fun all(): List<ReferenceData> = listOf(
        OWNER_ADDRESS_TYPE,
        AP_ADDRESS_TYPE,
        MAIN_ADDRESS_STATUS,
        PREV_ADDRESS_STATUS,
        NHC_Q001,
        NHC_Q002,
        NHC_Q005,
        NHC_Q049,
        NHC_Q095,
        NHC_Q701,
        NHC_Q702,
        NHC_Q703,
        NHC_Q704,
        NHC_Q705,
        NHC_Q706,
        NHC_Q707,
        NHC_Q708,
        NHC_Q709,
        NHC_Q710,
        NHC_Q711,
        NHC_Q712,
        NHC_Q713,
        NHC_Q714,
        NHC_Q715,
        NHC_Q716,
        STAFF_GRADE,
        REFERRAL_DATE_TYPE,
        REFERRAL_GROUP,
        ACCEPTED_DEFERRED_ADMISSION,
        AP_REFERRAL_SOURCE,
        YN_UNKNOWN,
        RISK_UNKNOWN,
        ORDER_EXPIRED,
        NON_ARRIVAL,
        REFERRAL_COMPLETED,
        ETHNICITY_WHITE,
        GENDER_FEMALE,
        GENDER_MALE,
        GENDER_IDENTITY_PNS,
        NATIONALITY_BRITISH,
        RELIGION_OTHER,
        NON_MAPPA_CATEGORY,
    ) + REGISTER_CATEGORIES.values + REGISTER_LEVELS.values + REFERRAL_CATEGORIES.values

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
