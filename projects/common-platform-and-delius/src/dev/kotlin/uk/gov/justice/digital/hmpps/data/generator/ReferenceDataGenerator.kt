package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.ADDRESS_TYPE
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.GENDER
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.OM_ALLOCATION_REASON
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*

object ReferenceDataGenerator {

    val GENDER_FEMALE = generate(ReferenceData.GenderCode.FEMALE.deliusValue, GENDER.id, "Female")
    val GENDER_MALE = generate(ReferenceData.GenderCode.MALE.deliusValue, GENDER.id, "Male")
    val INITIAL_ALLOCATION = generate(
        ReferenceData.StandardRefDataCode.INITIAL_ALLOCATION.code,
        OM_ALLOCATION_REASON.id,
        "Initial Allocation"
    )
    val MAIN_ADDRESS_STATUS =
        generate(ReferenceData.StandardRefDataCode.ADDRESS_MAIN_STATUS.code, ADDRESS_STATUS.id, "Main")
    val AWAITING_ASSESSMENT =
        generate(ReferenceData.StandardRefDataCode.AWAITING_ASSESSMENT.code, ADDRESS_TYPE.id, "Awaiting Assessment")

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