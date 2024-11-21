package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.ADDRESS_TYPE
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.COURT_APPEARANCE_OUTCOME
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.COURT_APPEARANCE_TYPE
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.GENDER
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.OM_ALLOCATION_REASON
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.ORDER_ALLOCATION_REASON
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.PLEA
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.REMAND_STATUS
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*

object ReferenceDataGenerator {

    val GENDER_FEMALE = generate(ReferenceData.GenderCode.FEMALE.deliusValue, GENDER.id, "Female")
    val GENDER_MALE = generate(ReferenceData.GenderCode.MALE.deliusValue, GENDER.id, "Male")
    val INITIAL_ALLOCATION = generate(
        ReferenceData.StandardRefDataCode.INITIAL_ALLOCATION.code,
        OM_ALLOCATION_REASON.id,
        "Initial Allocation"
    )
    val ORDER_MANAGER_INITIAL_ALLOCATION = generate(
        ReferenceData.StandardRefDataCode.INITIAL_ALLOCATION.code,
        ORDER_ALLOCATION_REASON.id,
        "Initial Allocation"
    )
    val MAIN_ADDRESS_STATUS =
        generate(ReferenceData.StandardRefDataCode.ADDRESS_MAIN_STATUS.code, ADDRESS_STATUS.id, "Main")
    val AWAITING_ASSESSMENT =
        generate(ReferenceData.StandardRefDataCode.AWAITING_ASSESSMENT.code, ADDRESS_TYPE.id, "Awaiting Assessment")
    val GUILTY_PLEA = generate(
        ReferenceData.StandardRefDataCode.GUILTY.code,
        PLEA.id,
        "Guilty"
    )
    val TRIAL_ADJOURNMENT_APPEARANCE_TYPE = generate(
        ReferenceData.StandardRefDataCode.TRIAL_ADJOURNMENT_APPEARANCE.code,
        COURT_APPEARANCE_TYPE.id,
        "Trial/Adjournment"
    )
    val REMANDED_IN_CUSTODY_OUTCOME = generate(
        ReferenceData.StandardRefDataCode.REMANDED_IN_CUSTODY_OUTCOME.code,
        COURT_APPEARANCE_OUTCOME.id,
        "Remanded in custody"
    )
    val REMANDED_IN_CUSTODY_STATUS = generate(
        ReferenceData.StandardRefDataCode.REMANDED_IN_CUSTODY_STATUS.code,
        REMAND_STATUS.id,
        "Remanded in custody"
    )

    fun generate(
        code: String,
        datasetId: Long,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(id = id, code = code, datasetId = datasetId, description = description)
}

object DatasetGenerator {
    val ALL_DATASETS = DatasetCode.entries.map { Dataset(IdGenerator.getAndIncrement(), it) }.associateBy { it.code }
    val GENDER = ALL_DATASETS[DatasetCode.GENDER]!!
    val OM_ALLOCATION_REASON = ALL_DATASETS[DatasetCode.OM_ALLOCATION_REASON]!!
    val ADDRESS_TYPE = ALL_DATASETS[DatasetCode.ADDRESS_TYPE]!!
    val ADDRESS_STATUS = ALL_DATASETS[DatasetCode.ADDRESS_STATUS]!!
    val ORDER_ALLOCATION_REASON = ALL_DATASETS[DatasetCode.ORDER_ALLOCATION_REASON]!!
    val COURT_APPEARANCE_TYPE = ALL_DATASETS[DatasetCode.COURT_APPEARANCE_TYPE]!!
    val REMAND_STATUS = ALL_DATASETS[DatasetCode.REMAND_STATUS]!!
    val PLEA = ALL_DATASETS[DatasetCode.PLEA]!!
    val COURT_APPEARANCE_OUTCOME = ALL_DATASETS[DatasetCode.COURT_APPEARANCE_OUTCOME]!!
}

object TransferReasonGenerator {
    val CASE_ORDER = generate(TransferReason.Reason.CASE_ORDER.code)
    fun generate(code: String, id: Long = IdGenerator.getAndIncrement()) = TransferReason(id, code)
}

object CourtGenerator {
    val UNKNOWN_COURT_N07_PROVIDER = generate(code = "UNKNCT", ouCode = "A00AA00")

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        selectable: Boolean = true,
        courtName: String = "Court not known",
        provider: Provider = ProviderGenerator.DEFAULT,
        ouCode: String? = null,
    ) = Court(
        id = id,
        code = code,
        selectable = selectable,
        name = courtName,
        provider = provider,
        ouCode = ouCode
    )
}

object ContactTypeGenerator {
    val EAPP = generate(
        code = ContactTypeCode.COURT_APPEARANCE.code,
        description = "Court Appearance"
    )
    fun generate(code: String, id: Long = IdGenerator.getAndIncrement(), description: String) =
        ContactType(id = id, code = code, description = description)
}