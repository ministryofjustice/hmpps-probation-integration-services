package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.GENDER
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator.OM_ALLOCATION_REASON
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*

object ReferenceDataGenerator {

    val GENDER_FEMALE = generate("F", GENDER.id, "Female")
    val GENDER_MALE = generate("M", GENDER.id, "Male")
    val INITIAL_ALLOCATION = generate("IN1", OM_ALLOCATION_REASON.id, "Initial Allocation")

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
}

object CourtGenerator {
    val UNKNOWN_COURT_N07_PROVIDER = generate(code = "UNKNCT", nationalCourtCode = "A00AA00")

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        selectable: Boolean = true,
        courtName: String = "Court not known",
        provider: Provider = ProviderGenerator.DEFAULT,
        nationalCourtCode: String? = null,
    ) = Court(
        id = id,
        code = code,
        selectable = selectable,
        courtName = courtName,
        probationArea = provider,
        nationalCourtCode = nationalCourtCode
    )
}