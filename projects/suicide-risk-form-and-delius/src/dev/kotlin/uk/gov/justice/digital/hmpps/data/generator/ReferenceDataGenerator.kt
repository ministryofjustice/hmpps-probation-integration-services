package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.ReferenceData

object ReferenceDataGenerator {
    fun generateDataset(code: String, id: Long = IdGenerator.getAndIncrement()) = Dataset(code, id)

    fun generateReferenceData(
        dataset: Dataset,
        code: String,
        description: String = "Description of $code",
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(
        id = id,
        code = code,
        description = description,
        dataset = dataset,
        selectable = selectable
    )
}