package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.event.sentence.Disposal
import uk.gov.justice.digital.hmpps.entity.event.sentence.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.event.sentence.LicenceConditionMainCategory
import java.time.LocalDate

object LicenceConditionGenerator {
    val RESIDENCE_MAIN_CATEGORY = LicenceConditionMainCategory(
        id = id(),
        code = "A",
        description = "Residence",
    )

    val CURFEW_MAIN_CATEGORY = LicenceConditionMainCategory(
        id = id(),
        code = "B",
        description = "Curfew",
    )

    val LENGTH_UNIT_EXAMPLE_SUB_CATEGORY = ReferenceData(
        id = id(),
        code = "SUB1",
        description = "Example sub category",
    )

    val CURFEW_SUB_CATEGORY = ReferenceData(
        id = id(),
        code = "CURFEW_SUB",
        description = "Curfew at specified address",
    )

    val YOUTH_ACTIVE_LICENCE_CONDITION = generateLicenceCondition(
        disposal = EventGenerator.YOUTH_EVENT_DISPOSAL,
        startDate = LocalDate.of(2026, 3, 1),
        mainCategory = CURFEW_MAIN_CATEGORY,
        subCategory = CURFEW_SUB_CATEGORY,
        notes = "Must be at residence between 21:00 and 06:00 daily",
        active = true,
        softDeleted = false,
    )

    fun generateLicenceCondition(
        disposal: Disposal,
        startDate: LocalDate = LocalDate.of(2026, 3, 1),
        mainCategory: LicenceConditionMainCategory = RESIDENCE_MAIN_CATEGORY,
        subCategory: ReferenceData? = null,
        notes: String? = "Some licence condition notes",
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = LicenceCondition(
        id = id,
        disposal = disposal,
        startDate = startDate,
        mainCategory = mainCategory,
        subCategory = subCategory,
        notes = notes,
        active = active,
        softDeleted = softDeleted,
    )
}