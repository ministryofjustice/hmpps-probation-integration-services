package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.component.category.LicenceConditionMainCategory
import java.time.ZonedDateTime

object LicenceConditionGenerator {
    fun generate(
        disposal: Disposal,
        mainCategory: LicenceConditionMainCategory,
        subCategory: ReferenceData? = null,
        startDate: ZonedDateTime = ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, EuropeLondon),
    ) = LicenceCondition(
        id = id(),
        mainCategory = mainCategory,
        subCategory = subCategory,
        disposal = disposal,
        startDate = startDate,
        active = true,
        softDeleted = false,
    )
}
