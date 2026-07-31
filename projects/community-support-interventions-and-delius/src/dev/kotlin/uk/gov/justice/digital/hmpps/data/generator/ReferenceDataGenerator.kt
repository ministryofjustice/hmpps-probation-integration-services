package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ReferenceData

object ReferenceDataGenerator {
    val GENDER_MALE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "M",
        description = "Male",
        datasetId = 0
    )
}