package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.court.entity.Court
import uk.gov.justice.digital.hmpps.integrations.delius.court.entity.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData

object ReferenceDataGenerator {
    val FLAG_DATASET = generateDataset(Dataset.Code.REGISTER_TYPE_FLAG.value)
    val DEFAULT_FLAG = generateReferenceData("1", dataset = FLAG_DATASET)
    val OFFENCES = listOf("80400").map { generateOffence(it) }

    val COURTS = listOf("CRT150").map { generateCourt(it) }

    fun generateReferenceData(
        code: String,
        description: String = "Description of $code",
        dataset: Dataset,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(code, description, dataset.id, id)

    fun generateDataset(code: String, id: Long = IdGenerator.getAndIncrement()) = Dataset(code, id)

    fun generateCourt(code: String, id: Long = IdGenerator.getAndIncrement()) = Court(code, id)

    fun generateOffence(code: String, id: Long = IdGenerator.getAndIncrement()) = Offence(code, id)
}