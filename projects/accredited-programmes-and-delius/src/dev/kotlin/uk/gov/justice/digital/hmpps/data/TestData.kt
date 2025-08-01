package uk.gov.justice.digital.hmpps.data

import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.entity.registration.RegisterType
import uk.gov.justice.digital.hmpps.entity.sentence.DisposalType
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceConditionMainCategory
import uk.gov.justice.digital.hmpps.entity.sentence.component.PssRequirementMainCategory
import uk.gov.justice.digital.hmpps.entity.sentence.component.PssRequirementSubCategory
import uk.gov.justice.digital.hmpps.entity.sentence.component.RequirementMainCategory
import uk.gov.justice.digital.hmpps.entity.sentence.custody.KeyDate
import uk.gov.justice.digital.hmpps.entity.sentence.offence.OffenceEntity
import uk.gov.justice.digital.hmpps.entity.staff.LocalAdminUnit
import uk.gov.justice.digital.hmpps.entity.staff.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.entity.staff.Team
import java.time.LocalDate

object TestData {
    val GENDER = ReferenceData(id(), "M", "Male")
    val ETHNICITY = ReferenceData(id(), "A9", "Asian or Asian British: Other")
    val MONTHS = ReferenceData(id(), "M", "Months")
    val ADULT_LICENCE = ReferenceData(id(), "ADL", "Released on Adult Licence")
    val PDU = ProbationDeliveryUnit(id(), "PDU1", "Test PDU")
    val LAU = LocalAdminUnit(id(), PDU)
    val TEAM = Team(id(), "Team1", "Test Team", LAU)
    val STAFF = StaffGenerator.generate()
    val PERSON = PersonGenerator.generate("A000001", GENDER, ETHNICITY)
    val MANAGER = ManagerGenerator.generate(PERSON, STAFF, TEAM)
    val USER = UserGenerator.generate("TestUser", STAFF)
    val USER_WITH_LIMITED_ACCESS = UserGenerator.generate("TestUserWithLimitedAccess")
    val RESTRICTION = RestrictionGenerator.generate(PERSON, USER)
    val EXCLUSION = ExclusionGenerator.generate(PERSON, USER_WITH_LIMITED_ACCESS)

    val ADULT_CUSTODY_TYPE = DisposalType(id(), "ORA Adult Custody (inc PSS)", "SC")
    val CUSTODIAL_EVENT = EventGenerator.generate(PERSON, 1)
    val CUSTODIAL_SENTENCE = DisposalGenerator.generate(CUSTODIAL_EVENT, ADULT_CUSTODY_TYPE, 24, MONTHS)
    val CUSTODY = CustodyGenerator.generate(CUSTODIAL_SENTENCE)
    val RELEASE = ReleaseGenerator.generate(CUSTODY, ADULT_LICENCE)

    val UNSENTENCED_EVENT = EventGenerator.generate(PERSON, 2)

    val COMMUNITY_ORDER_TYPE = DisposalType(id(), "ORA Community Order", "SP")
    val COMMUNITY_EVENT = EventGenerator.generate(PERSON, 3)
    val COMMUNITY_SENTENCE = DisposalGenerator.generate(COMMUNITY_EVENT, COMMUNITY_ORDER_TYPE, 6, MONTHS)

    val TWO_THIRDS_CONTACT_TYPE = ContactType(id(), ContactType.SUPERVISION_TWO_THIRDS_POINT)
    val OTHER_CONTACT_TYPE = ContactType(id(), "OTHER")
    val TWO_THIRDS_CONTACT =
        ContactGenerator.generate(CUSTODIAL_EVENT, TWO_THIRDS_CONTACT_TYPE, LocalDate.of(2067, 1, 1))
    val OTHER_CONTACT = ContactGenerator.generate(CUSTODIAL_EVENT, OTHER_CONTACT_TYPE, LocalDate.of(2000, 1, 1))

    val PSS_END_DATE_KEY_DATE_TYPE =
        ReferenceData(id(), KeyDate.POST_SENTENCE_SUPERVISION_END_DATE, "Post-sentence supervision end date")
    val PSS_END_DATE = KeyDateGenerator.generate(CUSTODY, PSS_END_DATE_KEY_DATE_TYPE, LocalDate.of(2100, 1, 1))
    val LED_KEY_DATE_TYPE =
        ReferenceData(id(), KeyDate.LICENCE_EXPIRY_DATE, "Post-sentence supervision end date")
    val LED_DATE = KeyDateGenerator.generate(CUSTODY, LED_KEY_DATE_TYPE, LocalDate.of(2050, 1, 1))

    val PSS_MAIN_TYPE = PssRequirementMainCategory(id(), "S09", "Drug Testing")
    val PSS_SUB_TYPE = PssRequirementSubCategory(id(), "TEST", "Pass drug tests")
    val PSS_REQUIREMENTS = listOf(
        PssRequirementGenerator.generate(CUSTODY, PSS_MAIN_TYPE),
        PssRequirementGenerator.generate(CUSTODY, PSS_MAIN_TYPE, PSS_SUB_TYPE)
    )
    val LICENCE_CONDITION_MAIN_TYPE = LicenceConditionMainCategory(id(), "NLC8", "Freedom of movement")
    val LICENCE_CONDITION_SUB_TYPE = ReferenceData(id(), "TEST", "To only attend specific places.")
    val LICENCE_CONDITIONS = listOf(
        LicenceConditionGenerator.generate(CUSTODIAL_SENTENCE, LICENCE_CONDITION_MAIN_TYPE),
        LicenceConditionGenerator.generate(CUSTODIAL_SENTENCE, LICENCE_CONDITION_MAIN_TYPE, LICENCE_CONDITION_SUB_TYPE),
    )
    val REQUIREMENT_MAIN_TYPE = RequirementMainCategory(id(), "H", "Alcohol Treatment")
    val REQUIREMENT_SUB_TYPE = ReferenceData(id(), "ALCTRT", "Alcohol Treatment")
    val REQUIREMENTS = listOf(
        RequirementGenerator.generate(COMMUNITY_SENTENCE, REQUIREMENT_MAIN_TYPE),
        RequirementGenerator.generate(COMMUNITY_SENTENCE, REQUIREMENT_MAIN_TYPE, REQUIREMENT_SUB_TYPE),
    )

    val OFFENCES = listOf(
        OffenceEntity(id(), "036", "Kidnapping", "02", "Hijacking"),
        OffenceEntity(id(), "036", "Kidnapping", "03", "False Imprisonment")
    )
    val MAIN_OFFENCE = MainOffenceGenerator.generate(CUSTODIAL_EVENT, OFFENCES[0])
    val ADDITIONAL_OFFENCE = AdditionalOffenceGenerator.generate(CUSTODIAL_EVENT, OFFENCES[1])

    val REGISTER_TYPE = RegisterType(id(), "RVHR", "Very High RoSH")
    val REGISTER_CATEGORY = ReferenceData(id(), "I3", "IOM - Fixed")
    val REGISTRATION = RegistrationGenerator.generate(PERSON, REGISTER_TYPE, REGISTER_CATEGORY)
}