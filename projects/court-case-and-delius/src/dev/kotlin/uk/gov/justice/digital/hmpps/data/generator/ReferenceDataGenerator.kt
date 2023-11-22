package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.AdRequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.LicenceConditionMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.RequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.NsiType

object ReferenceDataGenerator {
    val DISPOSAL_TYPE = ReferenceData(
        "D1",
        "Disposal type",
        IdGenerator.getAndIncrement()
    )
    val CUSTODIAL_STATUS = ReferenceData(
        "C1",
        "Custodial status",
        IdGenerator.getAndIncrement()
    )
    val LENGTH_UNITS = ReferenceData(
        "U1",
        "Days",
        IdGenerator.getAndIncrement()
    )
    val TERMINATION_REASON = ReferenceData(
        "R1",
        "Released",
        IdGenerator.getAndIncrement()
    )

    val REQUIREMENT_MAIN_CAT = RequirementMainCategory(
        "Main",
        "Main cat",
        IdGenerator.getAndIncrement()
    )

    val REQUIREMENT_SUB_CAT = ReferenceData(
        "Sub",
        "Sub cat",
        IdGenerator.getAndIncrement()
    )

    val AD_REQUIREMENT_MAIN_CAT = AdRequirementMainCategory(
        "AdMain",
        "AdMain cat",
        IdGenerator.getAndIncrement()
    )

    val AD_REQUIREMENT_SUB_CAT = ReferenceData(
        "AdSub",
        "AdSub cat",
        IdGenerator.getAndIncrement()
    )

    val LIC_COND_MAIN_CAT = LicenceConditionMainCategory(
        IdGenerator.getAndIncrement(),
        "LicMain",
        "lic cond main"
    )

    val LIC_COND_SUB_CAT = ReferenceData(
        "LicSub",
        "Lic Sub cat",
        IdGenerator.getAndIncrement()
    )

    val NSI_TYPE = NsiType(IdGenerator.getAndIncrement(), "NSI type", "NSI Type desc")
    val NSI_BREACH_OUTCOME = ReferenceData(
        "BRE01",
        "this NSI is in breach",
        IdGenerator.getAndIncrement()
    )
}
