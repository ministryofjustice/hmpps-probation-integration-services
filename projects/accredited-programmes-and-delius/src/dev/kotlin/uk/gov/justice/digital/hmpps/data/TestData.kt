package uk.gov.justice.digital.hmpps.data

import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.contact
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.Dataset
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.contact.ContactOutcome
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.entity.contact.enforcement.EnforcementAction
import uk.gov.justice.digital.hmpps.entity.registration.RegisterType
import uk.gov.justice.digital.hmpps.entity.sentence.DisposalType
import uk.gov.justice.digital.hmpps.entity.sentence.component.category.LicenceConditionMainCategory
import uk.gov.justice.digital.hmpps.entity.sentence.component.category.PssRequirementMainCategory
import uk.gov.justice.digital.hmpps.entity.sentence.component.category.PssRequirementSubCategory
import uk.gov.justice.digital.hmpps.entity.sentence.component.category.RequirementMainCategory
import uk.gov.justice.digital.hmpps.entity.sentence.component.manager.LicenceConditionManager
import uk.gov.justice.digital.hmpps.entity.sentence.component.manager.RequirementManager
import uk.gov.justice.digital.hmpps.entity.sentence.custody.KeyDate
import uk.gov.justice.digital.hmpps.entity.sentence.offence.OffenceEntity
import uk.gov.justice.digital.hmpps.entity.staff.*
import uk.gov.justice.digital.hmpps.integration.StatusInfo
import java.time.LocalDate
import java.time.ZonedDateTime

object TestData {
    val DATASET = Dataset(id(), "OTHER")
    val GENDER = ReferenceData(id(), "M", "Male", DATASET)
    val ETHNICITY = ReferenceData(id(), "A9", "Asian or Asian British: Other", DATASET)
    val MONTHS = ReferenceData(id(), "M", "Months", DATASET)
    val ADULT_LICENCE = ReferenceData(id(), "ADL", "Released on Adult Licence", DATASET)
    val PENDING_STATUS = ReferenceData(id(), "PN", "Pending", DATASET)
    val PROVIDER = Provider(id(), "PA1", "Test Provider")
    val PDU = ProbationDeliveryUnit(id(), "PDU1", "Test PDU", PROVIDER.id, true)
    val PDU_2 = ProbationDeliveryUnit(id(), "PDU2", "A Second PDU", PROVIDER.id, true)
    val LAU = LocalAdminUnit(id(), PDU, true)
    val LAU_2 = LocalAdminUnit(id(), PDU_2, true)
    val OFFICE_LOCATION = OfficeLocation(id(), "OFFICE1", "Test Office Location", null, LAU)
    val TEAM = Team(id(), "TEAM01", "Test Team", LAU, listOf(OFFICE_LOCATION), PROVIDER, null)
    val TEAM_2 = Team(id(), "TEAM02", "A Second Team", LAU_2, listOf(), PROVIDER, null)
    val TEAM_3 = Team(id(), "TEAM03", "A Third Team", LAU_2, listOf(), PROVIDER, null)
    val STAFF = StaffGenerator.generate("STAFF01", teams = listOf(TEAM))
    val STAFF_2 = StaffGenerator.generate("STAFF02", teams = listOf(TEAM_2))
    val STAFF_3 = StaffGenerator.generate("STAFF03", teams = listOf(TEAM_3))
    val STAFF_4 = StaffGenerator.generate("STAFF04", teams = listOf(TEAM_3))
    val PERSON = PersonGenerator.generate("A000001", GENDER, ETHNICITY)
    val MANAGER = ManagerGenerator.generate(PERSON, STAFF, TEAM)
    val CA_PERSON = PersonGenerator.generate("A000002", GENDER, ETHNICITY)
    val TERMINATION_PERSON = PersonGenerator.generate("A000003", GENDER, ETHNICITY)
    val TERMINATION_PERSON_MANAGER = ManagerGenerator.generate(TERMINATION_PERSON, STAFF, TEAM)
    val USER = UserGenerator.generate("TestUser", STAFF)
    val USER_WITH_LIMITED_ACCESS = UserGenerator.generate("TestUserWithLimitedAccess")
    val RESTRICTION = RestrictionGenerator.generate(PERSON, USER)
    val EXCLUSION = ExclusionGenerator.generate(PERSON, USER_WITH_LIMITED_ACCESS)

    val REFERENCE_DATA_LIST = listOf(
        ReferenceData.REQUIREMENT_COMPLETED,
        ReferenceData.LICENCE_CONDITION_COMPLETED,
        ReferenceData.REJECTED_STATUS,
        ReferenceData.REJECTED_DECISION,
        ReferenceData.LICENCE_CONDITION_TRANSFER_REJECTION_REASON,
        ReferenceData.REQUIREMENT_TRANSFER_REJECTION_REASON,
    )
    val DATASETS = REFERENCE_DATA_LIST.map { it.datasetCode }.distinct().associateWith { Dataset(id(), it) }
    val REFERENCE_DATA = REFERENCE_DATA_LIST
        .map { ReferenceData(id(), it.code, "Description of ${it.code}", DATASETS[it.datasetCode]!!) }

    val ADULT_CUSTODY_TYPE = DisposalType(id(), "ORA Adult Custody (inc PSS)", "SC", 1)
    val CUSTODIAL_EVENT = EventGenerator.generate(PERSON, 1)
    val CUSTODIAL_SENTENCE = DisposalGenerator.generate(CUSTODIAL_EVENT, ADULT_CUSTODY_TYPE, 24, MONTHS)
    val CUSTODY = CustodyGenerator.generate(CUSTODIAL_SENTENCE)
    val RELEASE = ReleaseGenerator.generate(CUSTODY, ADULT_LICENCE)
    val TERMINATION_CUSTODIAL_EVENT = EventGenerator.generate(TERMINATION_PERSON, 1)
    val TERMINATION_CUSTODIAL_SENTENCE =
        DisposalGenerator.generate(TERMINATION_CUSTODIAL_EVENT, ADULT_CUSTODY_TYPE, 24, MONTHS)

    val UNSENTENCED_EVENT = EventGenerator.generate(PERSON, 2)

    val COMMUNITY_ORDER_TYPE = DisposalType(id(), "ORA Community Order", "SP", 1)
    val COMMUNITY_EVENT = EventGenerator.generate(PERSON, 3)
    val COMMUNITY_SENTENCE = DisposalGenerator.generate(COMMUNITY_EVENT, COMMUNITY_ORDER_TYPE, 6, MONTHS)
    val CA_COMMUNITY_EVENT = EventGenerator.generate(CA_PERSON, 1)
    val CA_COMMUNITY_SENTENCE = DisposalGenerator.generate(CA_COMMUNITY_EVENT, COMMUNITY_ORDER_TYPE, 6, MONTHS)
    val TERMINATION_COMMUNITY_EVENT = EventGenerator.generate(TERMINATION_PERSON, 1)
    val TERMINATION_COMMUNITY_SENTENCE =
        DisposalGenerator.generate(TERMINATION_COMMUNITY_EVENT, COMMUNITY_ORDER_TYPE, 6, MONTHS)

    val TWO_THIRDS_CONTACT_TYPE = ContactType(id(), ContactType.SUPERVISION_TWO_THIRDS_POINT, false)
    val OTHER_CONTACT_TYPE = ContactType(id(), "OTHER", false)
    val TWO_THIRDS_CONTACT =
        CUSTODIAL_EVENT.contact(TWO_THIRDS_CONTACT_TYPE, LocalDate.of(2067, 1, 1), STAFF, TEAM, PROVIDER)
    val OTHER_CONTACT = CUSTODIAL_EVENT.contact(OTHER_CONTACT_TYPE, LocalDate.of(2000, 1, 1), STAFF, TEAM, PROVIDER)

    val COMPONENT_TERMINATED_CONTACT_TYPE = ContactType(id(), ContactType.COMPONENT_TERMINATED, false)
    val COMPONENT_TRANSFER_REJECTED_CONTACT_TYPE = ContactType(id(), ContactType.COMPONENT_TRANSFER_REJECTED, false)
    val PRE_GROUP_ONE_TO_ONE_MEETING_CONTACT_TYPE = ContactType(id(), ContactType.PRE_GROUP_ONE_TO_ONE_MEETING, false)
    val ORDER_COMPONENT_COMMENCED_CONTACT_TYPE = ContactType(id(), ContactType.ORDER_COMPONENT_COMMENCED, false)


    val ATTENDED_COMPLIED = ContactOutcome(id(), "ATTC", "Attended and Complied")
    val FAILED_TO_COMPLY = ContactOutcome(id(), "FTC", "Failed to comply", attended = false, complied = false)
    val REFER_TO_MANAGER_CONTACT_TYPE = ContactType(id(), "ROM", false)
    val REFER_TO_MANAGER_ACTION = EnforcementAction("ROM", "Refer to manager", 7, REFER_TO_MANAGER_CONTACT_TYPE, id())
    val ENFORCEMENT_REVIEW_CONTACT_TYPE = ContactType(id(), "ARWS", false)

    val PSS_END_DATE_KEY_DATE_TYPE =
        ReferenceData(id(), KeyDate.POST_SENTENCE_SUPERVISION_END_DATE, "Post-sentence supervision end date", DATASET)
    val PSS_END_DATE = KeyDateGenerator.generate(CUSTODY, PSS_END_DATE_KEY_DATE_TYPE, LocalDate.of(2100, 1, 1))
    val LED_KEY_DATE_TYPE = ReferenceData(id(), KeyDate.LICENCE_EXPIRY_DATE, "Licence expiry date", DATASET)
    val LED_DATE = KeyDateGenerator.generate(CUSTODY, LED_KEY_DATE_TYPE, LocalDate.of(2050, 1, 1))

    val PSS_MAIN_TYPE = PssRequirementMainCategory(id(), "S09", "Drug Testing")
    val PSS_SUB_TYPE = PssRequirementSubCategory(id(), "TEST", "Pass drug tests")
    val PSS_REQUIREMENTS = listOf(
        PssRequirementGenerator.generate(CUSTODY, PSS_MAIN_TYPE),
        PssRequirementGenerator.generate(CUSTODY, PSS_MAIN_TYPE, PSS_SUB_TYPE)
    )
    val LICENCE_CONDITION_MAIN_TYPE = LicenceConditionMainCategory(id(), "NLC8", "Freedom of movement")
    val LICENCE_CONDITION_SUB_TYPE = ReferenceData(id(), "TEST", "To only attend specific places.", DATASET)
    val LICENCE_CONDITIONS = listOf(
        LicenceConditionGenerator.generate(CUSTODIAL_SENTENCE, LICENCE_CONDITION_MAIN_TYPE),
        LicenceConditionGenerator.generate(CUSTODIAL_SENTENCE, LICENCE_CONDITION_MAIN_TYPE, LICENCE_CONDITION_SUB_TYPE),
    )
    val LICENCE_CONDITION_MANAGERS = LICENCE_CONDITIONS.map { LicenceConditionManager(id(), it, STAFF, TEAM) }

    val TERMINATION_LICENCE_CONDITION = LicenceConditionGenerator
        .generate(TERMINATION_CUSTODIAL_SENTENCE, LICENCE_CONDITION_MAIN_TYPE, LICENCE_CONDITION_SUB_TYPE)
    val TERMINATION_LICENCE_CONDITION_MANAGER =
        LicenceConditionManager(id(), TERMINATION_LICENCE_CONDITION, STAFF, TEAM)

    val REQUIREMENT_MAIN_TYPE = RequirementMainCategory(id(), "H", "Alcohol Treatment")
    val REQUIREMENT_SUB_TYPE = ReferenceData(id(), "ALCTRT", "Alcohol Treatment", DATASET)
    val REQUIREMENTS = listOf(
        RequirementGenerator.generate(COMMUNITY_SENTENCE, REQUIREMENT_MAIN_TYPE),
        RequirementGenerator.generate(COMMUNITY_SENTENCE, REQUIREMENT_MAIN_TYPE, REQUIREMENT_SUB_TYPE),
        RequirementGenerator.generate(CA_COMMUNITY_SENTENCE, REQUIREMENT_MAIN_TYPE, REQUIREMENT_SUB_TYPE),
    )
    val REQUIREMENT_MANAGERS = REQUIREMENTS.map { RequirementManager(id(), it, STAFF, TEAM) }

    val TERMINATION_REQUIREMENTS = listOf(
        RequirementGenerator.generate(TERMINATION_COMMUNITY_SENTENCE, REQUIREMENT_MAIN_TYPE, REQUIREMENT_SUB_TYPE),
        RequirementGenerator.generate(
            TERMINATION_COMMUNITY_SENTENCE,
            REQUIREMENT_MAIN_TYPE,
            startDate = ZonedDateTime.of(2030, 1, 1, 12, 0, 0, 0, EuropeLondon),
        ),
        RequirementGenerator.generate(
            TERMINATION_COMMUNITY_SENTENCE,
            REQUIREMENT_MAIN_TYPE,
            terminationDate = ZonedDateTime.of(2030, 1, 1, 12, 0, 0, 0, EuropeLondon)
        ),
        RequirementGenerator.generate(
            TERMINATION_COMMUNITY_SENTENCE,
            REQUIREMENT_MAIN_TYPE,
            REQUIREMENT_SUB_TYPE,
            pendingTransfer = true
        )
    )
    val TERMINATION_REQUIREMENT_MANAGERS = TERMINATION_REQUIREMENTS.map { RequirementManager(id(), it, STAFF, TEAM) }
    val TERMINATION_CONTACT = TERMINATION_REQUIREMENTS[2]
        .contact(COMPONENT_TERMINATED_CONTACT_TYPE, LocalDate.of(2030, 1, 1), STAFF, TEAM, PROVIDER)

    val REQUIREMENT_TRANSFER =
        RequirementTransferGenerator.generate(TERMINATION_REQUIREMENTS[3], PENDING_STATUS, TEAM, STAFF)

    val OFFENCES = listOf(
        OffenceEntity(id(), "036", "Kidnapping", "02", "Hijacking"),
        OffenceEntity(id(), "036", "Kidnapping", "03", "False Imprisonment")
    )
    val MAIN_OFFENCE = MainOffenceGenerator.generate(CUSTODIAL_EVENT, OFFENCES[0])
    val ADDITIONAL_OFFENCE = AdditionalOffenceGenerator.generate(CUSTODIAL_EVENT, OFFENCES[1])

    val REGISTER_TYPE = RegisterType(id(), "RVHR", "Very High RoSH")
    val REGISTER_CATEGORY = ReferenceData(id(), "I3", "IOM - Fixed", DATASET)
    val REGISTRATION = RegistrationGenerator.generate(PERSON, REGISTER_TYPE, REGISTER_CATEGORY)

    val APPOINTMENT_CONTACT_TYPE = ContactType(id(), ContactType.APPOINTMENT, true)
    val STATUS_CONTACT_TYPES = StatusInfo.Status.entries.map { ContactType(id(), it.contactTypeCode, false) }
    val THREE_WAY_MEETING_TYPE = ContactType(id(), ContactType.THREE_WAY_MEETING, false)

    val APPOINTMENTS = REQUIREMENTS.take(2).mapIndexed { idx, r ->
        r.contact(APPOINTMENT_CONTACT_TYPE, LocalDate.of(2030, 1, 1 + idx), STAFF, TEAM, PROVIDER)
    } + LICENCE_CONDITIONS.take(2).mapIndexed { idx, lc ->
        lc.contact(APPOINTMENT_CONTACT_TYPE, LocalDate.of(2030, 1, 1 + idx), STAFF, TEAM, PROVIDER)
    }

    val DOMAIN_EVENT_DATASET = Dataset(id(), "DOMAIN EVENT TYPE")
    val DOMAIN_EVENT_TYPES = listOf(
        "probation-case.licence-condition.terminated",
        "probation-case.requirement.terminated"
    ).map { type -> ReferenceData(id(), type, type, DOMAIN_EVENT_DATASET) }
}