package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Dataset
import uk.gov.justice.digital.hmpps.entity.ReferenceData

object ReferenceDataGenerator {
    val MR_TITLE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "Mr",
        description = "Mr",
        dataset = Dataset("TITLE", IdGenerator.getAndIncrement()),
        selectable = true,
        linkedData = emptySet()
    )
    val MAIN_ADDRESS_STATUS = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "MAIN",
        description = "Main",
        dataset = Dataset("ADDRESS STATUS", IdGenerator.getAndIncrement()),
        selectable = true,
        linkedData = emptySet()
    )
}