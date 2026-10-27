package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.EVENT
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator.generateReferenceData
import uk.gov.justice.digital.hmpps.integration.delius.entity.CreateAndVaryALicenceMapping
import uk.gov.justice.digital.hmpps.integration.delius.entity.LicenceCondition
import uk.gov.justice.digital.hmpps.integration.delius.entity.LicenceConditionMainCategory
import java.time.ZonedDateTime

object LicenceConditionGenerator {
    val MAIN_CATEGORY = LicenceConditionMainCategory(id(), "LC01", "Licence condition main category")
    val SUB_CATEGORY = generateReferenceData("LCS01", "Licence condition subcategory")
    val CVL_MAPPING = CreateAndVaryALicenceMapping(
        id = id(),
        cvlCode = "CVL01",
        mainCategory = MAIN_CATEGORY,
        subCategory = SUB_CATEGORY
    )
    val LICENCE_CONDITION = LicenceCondition(
        id = id(),
        disposal = checkNotNull(EVENT.disposal),
        mainCategory = MAIN_CATEGORY,
        subCategory = SUB_CATEGORY,
        createdDateTime = ZonedDateTime.parse("2026-01-01T10:00:00Z"),
        lastUpdatedDateTime = ZonedDateTime.parse("2026-01-02T10:00:00Z")
    )
}
