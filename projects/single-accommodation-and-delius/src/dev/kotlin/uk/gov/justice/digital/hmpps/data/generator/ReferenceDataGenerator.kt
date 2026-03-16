package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.ReferenceData

object ReferenceDataGenerator {
    val GENDER_MALE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "M",
        description = "Male"
    )

    val C1_TIER = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "C1",
        description = "C1"
    )

    val EXP_RELEASE_DATE_TYPE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "EXP",
        description = "Expected Release Date"
    )
}