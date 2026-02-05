package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.ReferenceData

object ReferenceDataGenerator {
    val DEFAULT_TITLE = ReferenceData(IdGenerator.getAndIncrement(), "MR", "Mr", generateDataset("title"), true)
    val DEFAULT_STATUS =
        ReferenceData(IdGenerator.getAndIncrement(), "active", "Active", generateDataset("status"), true)
    val DEFAULT_ADDRESS_STATUS =
        ReferenceData(IdGenerator.getAndIncrement(), "CURRENT", "CURRENT", generateDataset("address_status"), true)

    fun generateDataset(code: String, id: Long = IdGenerator.getAndIncrement()) = Dataset(code, id)
}
