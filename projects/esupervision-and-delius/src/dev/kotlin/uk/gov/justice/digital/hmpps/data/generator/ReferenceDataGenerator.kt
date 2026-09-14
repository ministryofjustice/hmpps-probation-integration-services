package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.ReferenceData

object ReferenceDataGenerator {
    val LENGTH_UNIT_MONTHS = ReferenceData(
        id = id(),
        code = "M",
        description = "Months",
    )
}

