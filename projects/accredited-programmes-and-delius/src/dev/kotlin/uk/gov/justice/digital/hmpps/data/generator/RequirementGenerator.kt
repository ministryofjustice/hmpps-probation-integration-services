package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement
import uk.gov.justice.digital.hmpps.entity.sentence.component.category.RequirementMainCategory
import java.time.ZonedDateTime

object RequirementGenerator {
    fun generate(
        disposal: Disposal,
        mainCategory: RequirementMainCategory,
        subCategory: ReferenceData? = null,
        startDate: ZonedDateTime = ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, EuropeLondon),
        terminationDate: ZonedDateTime? = null,
        pendingTransfer: Boolean = false
    ) = Requirement(
        id = id(),
        mainCategory = mainCategory,
        subCategory = subCategory,
        disposal = disposal,
        startDate = startDate,
        terminationDate = terminationDate,
        pendingTransfer = pendingTransfer,
        active = true,
        softDeleted = false,
    )
}
