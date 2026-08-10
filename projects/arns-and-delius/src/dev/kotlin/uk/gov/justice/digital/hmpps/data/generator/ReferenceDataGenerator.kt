package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ReferenceData

object ReferenceDataGenerator {
    val MALE = generate(
        code = "M",
        description = "Male",
    )

    fun generate(
        code: String,
        description: String,
        id: Long = IdGenerator.getAndIncrement(),
    ) = ReferenceData(
        id = id,
        code = code,
        description = description,
    )
}