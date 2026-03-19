package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.ReferenceDataSet

object ReferenceDataGenerator {
    val MISC_DATA_SET_ID = IdGenerator.getAndIncrement()
    val MR_TITLE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "Mr",
        description = "Mr",
        selectable = true,
        dataSetId = MISC_DATA_SET_ID
    )

    val DEFAULT_REGISTER_TYPE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "ALT7",
        description = "Suicide and Self Harm ALT7",
        selectable = true,
        dataSetId = MISC_DATA_SET_ID
    )

    val OTHER_REGISTER_TYPE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "CD98",
        description = "Some Other Registration",
        selectable = true,
        dataSetId = MISC_DATA_SET_ID
    )

    val DEFAULT_REGISTER_LEVEL = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "HIGH",
        description = "Register Level High",
        selectable = true,
        dataSetId = MISC_DATA_SET_ID
    )

    val DEFAULT_REGISTER_CATEGORY = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "CAT1",
        description = "Category 1",
        selectable = true,
        dataSetId = MISC_DATA_SET_ID
    )

    val MAIN_ADDRESS_STATUS = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "MAIN",
        description = "Main",
        selectable = true,
        dataSetId = MISC_DATA_SET_ID
    )
    val LENGTH_UNITS_MONTHS = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "MONTHS",
        description = "Months",
        selectable = true,
        dataSetId = MISC_DATA_SET_ID
    )
    val LENGTH_UNITS_DAYS = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "DAYS",
        description = "Days",
        selectable = true,
        dataSetId = MISC_DATA_SET_ID
    )

    val SENTENCE_APPEARANCE_TYPE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "S",
        description = "Sentence",
        selectable = true,
        dataSetId = MISC_DATA_SET_ID
    )

    val DEFAULT_OUTCOME = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "PR",
        description = "Probation",
        selectable = true,
        dataSetId = MISC_DATA_SET_ID
    )

    val DEFAULT_REQUIREMENT_SUBTYPE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "PR2",
        description = "Probation2",
        selectable = true,
        dataSetId = MISC_DATA_SET_ID
    )

    val BREACH_REASON_DATASET = ReferenceDataSet(
        id = IdGenerator.getAndIncrement(),
        name = "BREACH REASON"
    )

    val BREACH_REASON = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "Absent",
        description = "Unauthorised absence",
        selectable = true,
        dataSetId = BREACH_REASON_DATASET.id
    )

}