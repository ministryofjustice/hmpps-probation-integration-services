package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.DetailedOffence
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.ReferenceDataSet
import uk.gov.justice.digital.hmpps.entity.ReferenceOffence
import java.time.LocalDate

object DataGenerator {
    val COURT_CATEGORY_SET = ReferenceDataSet(IdGenerator.getAndIncrement(), "COURT CATEGORY")
    val COURT_CATEGORY = ReferenceData(IdGenerator.getAndIncrement(), "CS", "Summary Non-motoring", COURT_CATEGORY_SET)

    val EXISTING_DETAILED_OFFENCE = DetailedOffence(
        code = "AB06001",
        description = "Obstruct person acting in execution of the regulations - 09155",
        category = COURT_CATEGORY,
        homeOfficeCode = "091/55",
        homeOfficeDescription = "Obstruct person acting in execution of the regulations",
        legislation = "N/A",
        startDate = LocalDate.of(2006, 5, 12),
        endDate = null
    )

    val HIGH_LEVEL_OFFENCE = generateReferenceOffence(
        code = "09100",
        mainCategoryCode = "091",
        subCategoryCode = "00",
        ogrsOffenceCategoryId = 1L
    )
    val EXISTING_OFFENCE = generateReferenceOffence(
        code = "09155",
        mainCategoryCode = "091",
        subCategoryCode = "55",
        ogrsOffenceCategoryId = 2L
    )

    fun generateReferenceOffence(
        code: String,
        description: String = "Description of $code",
        selectable: Boolean = true,
        mainCategoryCode: String,
        mainCategoryDescription: String = "Main Category of $code",
        mainCategoryAbbreviation: String? = mainCategoryDescription.take(50),
        ogrsOffenceCategoryId: Long?,
        subCategoryCode: String,
        subCategoryDescription: String = "Sub Category of $code",
        form20Code: String? = null,
        schedule15SexualOffence: Boolean? = false,
        schedule15ViolentOffence: Boolean? = false,
        childAbduction: Boolean? = false
    ) = ReferenceOffence(
        code,
        description,
        selectable,
        mainCategoryCode,
        mainCategoryDescription,
        mainCategoryAbbreviation,
        ogrsOffenceCategoryId,
        subCategoryCode,
        subCategoryDescription,
        form20Code,
        schedule15SexualOffence,
        schedule15ViolentOffence,
        childAbduction
    )
}
