package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ReferenceData

object ReferenceDataGenerator {
    val MR_TITLE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "Mr",
        description = "Mr",
        selectable = true,
    )
    val MAIN_ADDRESS_STATUS = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "MAIN",
        description = "Main",
        selectable = true,
    )
}