package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.referencedata.ReferenceData

object ReferenceDataGenerator {
    val REGISTER_CATEGORY_M3 = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "M3",
        description = "MAPPA Category M3"
    )

    val GENDER_MALE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "M",
        description = "Male"
    )

    val EXP_RELEASE_DATE_TYPE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "EXP",
        description = "Expected Release Date"
    )
}