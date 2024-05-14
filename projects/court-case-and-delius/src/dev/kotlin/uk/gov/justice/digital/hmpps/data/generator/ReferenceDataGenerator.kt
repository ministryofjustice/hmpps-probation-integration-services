package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtReportType
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.AdRequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.LicenceConditionMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.RequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.NsiType
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.PssRequirementMainCat
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.PssRequirementSubCat

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

    val PSS_MAIN_CAT = PssRequirementMainCat(
        "PssMain",
        "pss main",
        IdGenerator.getAndIncrement()
    )

    val PSS_SUB_CAT = PssRequirementSubCat(
        "PssSub",
        "pss sub",
        IdGenerator.getAndIncrement()
    )

    val COURT_REPORT_TYPE = CourtReportType(
        "CR1",
        "court report",
        IdGenerator.getAndIncrement()
    )

    val GENDER_MALE = ReferenceData(
        "MALE",
        "Male",
        IdGenerator.getAndIncrement()
    )

    val PROVISION_TYPE_1 = ReferenceData(
        "PROV1",
        "Provision type 1",
        IdGenerator.getAndIncrement()
    )

    val PROVISION_CATEGORY_1 = ReferenceData(
        "PROV1",
        "Provision type 1",
        IdGenerator.getAndIncrement()
    )

    val DISABILITY_TYPE_1 = ReferenceData(
        "DIS1",
        "Disability type 1",
        IdGenerator.getAndIncrement()
    )

    val DISABILITY_CONDITION_1 = ReferenceData(
        "DISCON1",
        "Disability Condition 1",
        IdGenerator.getAndIncrement()
    )

    val GENDER_IDENTITY = ReferenceData(
        "GEN",
        "Some gender identity",
        IdGenerator.getAndIncrement()
    )

    val ETHNICITY = ReferenceData(
        "ETH",
        "Some ethnicity",
        IdGenerator.getAndIncrement()
    )

    val IMMIGRATION_STATUS = ReferenceData(
        "ETH",
        "Some immigration status",
        IdGenerator.getAndIncrement()
    )

    val NATIONALITY = ReferenceData(
        "BRIT",
        "British",
        IdGenerator.getAndIncrement()
    )

    val LANGUAGE_ENG = ReferenceData(
        "ENG",
        "English",
        IdGenerator.getAndIncrement()
    )

    val RELIGION = ReferenceData(
        "REL",
        "A Relgion",
        IdGenerator.getAndIncrement()
    )

    val SECOND_NATIONALITY = ReferenceData(
        "FRE",
        "French",
        IdGenerator.getAndIncrement()
    )

    val SEXUAL_ORIENTATION = ReferenceData(
        "SO",
        "A sexual orientation",
        IdGenerator.getAndIncrement()
    )

    val TITLE = ReferenceData(
        "MR",
        "Mr",
        IdGenerator.getAndIncrement()
    )
}
