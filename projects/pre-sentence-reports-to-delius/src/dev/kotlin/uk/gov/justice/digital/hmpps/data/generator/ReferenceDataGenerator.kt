package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ReferenceData

object ReferenceDataGenerator {
    val DEFAULT_TITLE = ReferenceData(IdGenerator.getAndIncrement(), "MR", "Mr", true)
    val DEFAULT_ADDRESS_STATUS =
        ReferenceData(IdGenerator.getAndIncrement(), "M", "Main", true)
}
