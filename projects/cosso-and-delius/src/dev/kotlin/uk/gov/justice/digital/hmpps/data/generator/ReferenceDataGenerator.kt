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
    val LENGTH_UNITS_MONTHS = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "MONTHS",
        description = "Months",
        selectable = true,
    )
    val LENGTH_UNITS_DAYS = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "DAYS",
        description = "Days",
        selectable = true,
    )

    val SENTENCE_APPEARANCE_TYPE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "S",
        description = "Sentence",
        selectable = true,
    )

    val DEFAULT_OUTCOME = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "PR",
        description = "Probation",
        selectable = true,
    )

    val DEFAULT_REQUIREMENT_SUBTYPE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "PR2",
        description = "Probation2",
        selectable = true,
    )
}