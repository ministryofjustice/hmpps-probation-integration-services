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

    val MONTHS = ReferenceData(
        "M",
        "months",
        IdGenerator.getAndIncrement()
    )

    val REQUIREMENT_MAIN_CAT = RequirementMainCategory(
        "Main",
        "Main cat",
        MONTHS,
        "N",
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

    val NSI_TYPE = NsiType(IdGenerator.getAndIncrement(), "NSITYPE", "NSI Type desc")
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

    val GENDER_FEMALE = ReferenceData(
        "FEMALE",
        "Female",
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

    val VIOLENCE = ReferenceData(
        "VI",
        "Violence",
        IdGenerator.getAndIncrement()
    )

    val HOURS_WORKED = ReferenceData(
        "HC",
        "Hours Completed",
        IdGenerator.getAndIncrement()
    )

    val REF_DISQ = ReferenceData(
        "DISQ",
        "Disqualified from Driving",
        IdGenerator.getAndIncrement()
    )

    val DEFAULT_ADDRESS_TYPE = ReferenceData(
        "AT",
        "Address Type",
        IdGenerator.getAndIncrement()
    )

    val DEFAULT_ADDRESS_STATUS = ReferenceData(
        "AS",
        "Address Status",
        IdGenerator.getAndIncrement()
    )

    val DEFAULT_ALLOCATION_REASON = ReferenceData(
        "AR",
        "Allocation Reason",
        IdGenerator.getAndIncrement()
    )

    val DEFAULT_TIER = ReferenceData(
        "B2",
        "B2",
        IdGenerator.getAndIncrement()
    )

    val PRISON = ReferenceData(
        "E",
        "prison",
        IdGenerator.getAndIncrement()
    )

    val ACR = ReferenceData(
        "ACR",
        "Auto-Conditional Release Date",
        IdGenerator.getAndIncrement()
    )

    val EXP = ReferenceData(
        "EXP",
        "Expected Release Date",
        IdGenerator.getAndIncrement()
    )

    val HDE = ReferenceData(
        "HDE",
        "HDC Expected Date",
        IdGenerator.getAndIncrement()
    )

    val LED = ReferenceData(
        "LED",
        "Licence Expiry Date",
        IdGenerator.getAndIncrement()
    )

    val PED = ReferenceData(
        "PED",
        "Parole Eligibility Date",
        IdGenerator.getAndIncrement()
    )

    val PSSED = ReferenceData(
        "PSSED",
        "Post-Sentence Supervision End Date",
        IdGenerator.getAndIncrement()
    )

    val POM1 = ReferenceData(
        "POM1",
        "POM Handover Expected Start Date",
        IdGenerator.getAndIncrement()
    )

    val POM2 = ReferenceData(
        "POM2",
        "RO responsibility handover from POM to OM Expected Date",
        IdGenerator.getAndIncrement()
    )

    val SED = ReferenceData(
        "SED",
        "Sentence Expiry Date",
        IdGenerator.getAndIncrement()
    )

    val CRN = ReferenceData(
        "CRN",
        "Crown Court",
        IdGenerator.getAndIncrement()
    )

    val TRIAL = ReferenceData(
        code = "T",
        "Trial/Adjournment",
        IdGenerator.getAndIncrement()
    )

    val REG_CATEGORY = ReferenceData(
        code = "RC1",
        "Registration Category",
        IdGenerator.getAndIncrement()
    )

    val REG_LEVEL = ReferenceData(
        code = "L1",
        "Registration Level",
        IdGenerator.getAndIncrement()
    )

    val REG_FLAG = ReferenceData(
        code = "F1",
        "Registration Flag",
        IdGenerator.getAndIncrement()
    )
}
