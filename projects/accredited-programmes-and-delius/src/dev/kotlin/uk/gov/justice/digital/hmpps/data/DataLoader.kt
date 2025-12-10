package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.TestData.ADDITIONAL_OFFENCE
import uk.gov.justice.digital.hmpps.data.TestData.ADULT_CUSTODY_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.ADULT_LICENCE
import uk.gov.justice.digital.hmpps.data.TestData.APPOINTMENTS
import uk.gov.justice.digital.hmpps.data.TestData.APPOINTMENT_CONTACT_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.ATTENDED_COMPLIED
import uk.gov.justice.digital.hmpps.data.TestData.CA_COMMUNITY_EVENT
import uk.gov.justice.digital.hmpps.data.TestData.CA_COMMUNITY_SENTENCE
import uk.gov.justice.digital.hmpps.data.TestData.CA_PERSON
import uk.gov.justice.digital.hmpps.data.TestData.COMMUNITY_EVENT
import uk.gov.justice.digital.hmpps.data.TestData.COMMUNITY_ORDER_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.COMMUNITY_SENTENCE
import uk.gov.justice.digital.hmpps.data.TestData.COMPONENT_TERMINATED_CONTACT_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.COMPONENT_TRANSFER_REJECTED_CONTACT_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.CUSTODIAL_EVENT
import uk.gov.justice.digital.hmpps.data.TestData.CUSTODIAL_SENTENCE
import uk.gov.justice.digital.hmpps.data.TestData.CUSTODY
import uk.gov.justice.digital.hmpps.data.TestData.DATASET
import uk.gov.justice.digital.hmpps.data.TestData.DATASETS
import uk.gov.justice.digital.hmpps.data.TestData.DOMAIN_EVENT_DATASET
import uk.gov.justice.digital.hmpps.data.TestData.DOMAIN_EVENT_TYPES
import uk.gov.justice.digital.hmpps.data.TestData.ENFORCEMENT_REVIEW_CONTACT_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.ETHNICITY
import uk.gov.justice.digital.hmpps.data.TestData.EXCLUSION
import uk.gov.justice.digital.hmpps.data.TestData.FAILED_TO_COMPLY
import uk.gov.justice.digital.hmpps.data.TestData.GENDER
import uk.gov.justice.digital.hmpps.data.TestData.LAU
import uk.gov.justice.digital.hmpps.data.TestData.LAU_2
import uk.gov.justice.digital.hmpps.data.TestData.LED_DATE
import uk.gov.justice.digital.hmpps.data.TestData.LED_KEY_DATE_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.LICENCE_CONDITIONS
import uk.gov.justice.digital.hmpps.data.TestData.LICENCE_CONDITION_MAIN_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.LICENCE_CONDITION_MANAGERS
import uk.gov.justice.digital.hmpps.data.TestData.LICENCE_CONDITION_SUB_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.MAIN_OFFENCE
import uk.gov.justice.digital.hmpps.data.TestData.MANAGER
import uk.gov.justice.digital.hmpps.data.TestData.MONTHS
import uk.gov.justice.digital.hmpps.data.TestData.OFFENCES
import uk.gov.justice.digital.hmpps.data.TestData.OFFICE_LOCATION
import uk.gov.justice.digital.hmpps.data.TestData.OTHER_CONTACT
import uk.gov.justice.digital.hmpps.data.TestData.OTHER_CONTACT_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.PDU
import uk.gov.justice.digital.hmpps.data.TestData.PDU_2
import uk.gov.justice.digital.hmpps.data.TestData.PENDING_STATUS
import uk.gov.justice.digital.hmpps.data.TestData.PERSON
import uk.gov.justice.digital.hmpps.data.TestData.PROVIDER
import uk.gov.justice.digital.hmpps.data.TestData.PSS_END_DATE
import uk.gov.justice.digital.hmpps.data.TestData.PSS_END_DATE_KEY_DATE_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.PSS_MAIN_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.PSS_REQUIREMENTS
import uk.gov.justice.digital.hmpps.data.TestData.PSS_SUB_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.REFERENCE_DATA
import uk.gov.justice.digital.hmpps.data.TestData.REFER_TO_MANAGER_ACTION
import uk.gov.justice.digital.hmpps.data.TestData.REFER_TO_MANAGER_CONTACT_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.REGISTER_CATEGORY
import uk.gov.justice.digital.hmpps.data.TestData.REGISTER_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.REGISTRATION
import uk.gov.justice.digital.hmpps.data.TestData.RELEASE
import uk.gov.justice.digital.hmpps.data.TestData.REQUIREMENTS
import uk.gov.justice.digital.hmpps.data.TestData.REQUIREMENT_MAIN_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.REQUIREMENT_MANAGERS
import uk.gov.justice.digital.hmpps.data.TestData.REQUIREMENT_SUB_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.REQUIREMENT_TRANSFER
import uk.gov.justice.digital.hmpps.data.TestData.RESTRICTION
import uk.gov.justice.digital.hmpps.data.TestData.STAFF
import uk.gov.justice.digital.hmpps.data.TestData.STAFF_2
import uk.gov.justice.digital.hmpps.data.TestData.STAFF_3
import uk.gov.justice.digital.hmpps.data.TestData.STAFF_4
import uk.gov.justice.digital.hmpps.data.TestData.STATUS_CONTACT_TYPES
import uk.gov.justice.digital.hmpps.data.TestData.TEAM
import uk.gov.justice.digital.hmpps.data.TestData.TEAM_2
import uk.gov.justice.digital.hmpps.data.TestData.TEAM_3
import uk.gov.justice.digital.hmpps.data.TestData.TERMINATION_COMMUNITY_EVENT
import uk.gov.justice.digital.hmpps.data.TestData.TERMINATION_COMMUNITY_SENTENCE
import uk.gov.justice.digital.hmpps.data.TestData.TERMINATION_CONTACT
import uk.gov.justice.digital.hmpps.data.TestData.TERMINATION_CUSTODIAL_EVENT
import uk.gov.justice.digital.hmpps.data.TestData.TERMINATION_CUSTODIAL_SENTENCE
import uk.gov.justice.digital.hmpps.data.TestData.TERMINATION_LICENCE_CONDITION
import uk.gov.justice.digital.hmpps.data.TestData.TERMINATION_LICENCE_CONDITION_MANAGER
import uk.gov.justice.digital.hmpps.data.TestData.TERMINATION_PERSON
import uk.gov.justice.digital.hmpps.data.TestData.TERMINATION_PERSON_MANAGER
import uk.gov.justice.digital.hmpps.data.TestData.TERMINATION_REQUIREMENTS
import uk.gov.justice.digital.hmpps.data.TestData.TERMINATION_REQUIREMENT_MANAGERS
import uk.gov.justice.digital.hmpps.data.TestData.THREE_WAY_MEETING_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.TWO_THIRDS_CONTACT
import uk.gov.justice.digital.hmpps.data.TestData.TWO_THIRDS_CONTACT_TYPE
import uk.gov.justice.digital.hmpps.data.TestData.UNSENTENCED_EVENT
import uk.gov.justice.digital.hmpps.data.TestData.USER
import uk.gov.justice.digital.hmpps.data.TestData.USER_WITH_LIMITED_ACCESS
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.repository.*

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val referenceDataRepository: ReferenceDataRepository,
    private val probationDeliveryUnitRepository: ProbationDeliveryUnitRepository,
    private val localAdminUnitRepository: LocalAdminUnitRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val personRepository: PersonRepository,
    private val managerRepository: ManagerRepository,
    private val userRepository: UserRepository,
    private val exclusionRepository: ExclusionRepository,
    private val restrictionRepository: RestrictionRepository,
    private val eventRepository: EventRepository,
    private val disposalTypeRepository: DisposalTypeRepository,
    private val disposalRepository: DisposalRepository,
    private val custodyRepository: CustodyRepository,
    private val releaseRepository: ReleaseRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val contactRepository: ContactRepository,
    private val keyDateRepository: KeyDateRepository,
    private val pssRequirementMainCategoryRepository: PssRequirementMainCategoryRepository,
    private val pssRequirementSubCategoryRepository: PssRequirementSubCategoryRepository,
    private val pssRequirementRepository: PssRequirementRepository,
    private val licenceConditionMainCategoryRepository: LicenceConditionMainCategoryRepository,
    private val licenceConditionRepository: LicenceConditionRepository,
    private val licenceConditionManagerRepository: LicenceConditionManagerRepository,
    private val requirementMainCategoryRepository: RequirementMainCategoryRepository,
    private val requirementRepository: RequirementRepository,
    private val requirementManagerRepository: RequirementManagerRepository,
    private val offenceRepository: OffenceEntityRepository,
    private val mainOffenceRepository: MainOffenceRepository,
    private val additionalOffenceRepository: AdditionalOffenceRepository,
    private val registerTypeRepository: RegisterTypeRepository,
    private val registrationRepository: RegistrationRepository,
    private val officeLocationRepository: OfficeLocationRepository,
    private val providerRepository: ProviderRepository,
    private val enforcementActionRepository: EnforcementActionRepository,
    private val datasetRepository: DatasetRepository,
    private val requirementTransferRepository: RequirementTransferRepository,
) : ApplicationListener<ApplicationReadyEvent> {
    @PostConstruct
    fun saveAuditUser() {
        userRepository.save(UserGenerator.generate("AccreditedProgrammesAndDelius"))
    }

    @Transactional
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        datasetRepository.save(DATASET)
        datasetRepository.save(DOMAIN_EVENT_DATASET)
        datasetRepository.saveAll(DATASETS.values)
        referenceDataRepository.save(GENDER)
        referenceDataRepository.save(ETHNICITY)
        referenceDataRepository.save(MONTHS)
        referenceDataRepository.save(ADULT_LICENCE)
        referenceDataRepository.save(PENDING_STATUS)
        referenceDataRepository.saveAll(DOMAIN_EVENT_TYPES)
        referenceDataRepository.saveAll(REFERENCE_DATA)
        providerRepository.save(PROVIDER)
        probationDeliveryUnitRepository.save(PDU)
        probationDeliveryUnitRepository.save(PDU_2)
        localAdminUnitRepository.save(LAU)
        localAdminUnitRepository.save(LAU_2)
        officeLocationRepository.save(OFFICE_LOCATION)
        teamRepository.save(TEAM)
        teamRepository.save(TEAM_2)
        teamRepository.save(TEAM_3)
        staffRepository.save(STAFF)
        staffRepository.save(STAFF_2)
        staffRepository.save(STAFF_3)
        staffRepository.save(STAFF_4)
        personRepository.save(PERSON)
        personRepository.save(CA_PERSON)
        personRepository.save(TERMINATION_PERSON)
        managerRepository.save(MANAGER)
        managerRepository.save(TERMINATION_PERSON_MANAGER)
        userRepository.save(USER)
        userRepository.save(USER_WITH_LIMITED_ACCESS)
        userRepository.flush()
        exclusionRepository.save(EXCLUSION)
        restrictionRepository.save(RESTRICTION)
        eventRepository.save(CUSTODIAL_EVENT)
        eventRepository.save(UNSENTENCED_EVENT)
        eventRepository.save(COMMUNITY_EVENT)
        eventRepository.save(CA_COMMUNITY_EVENT)
        eventRepository.save(TERMINATION_COMMUNITY_EVENT)
        eventRepository.save(TERMINATION_CUSTODIAL_EVENT)
        disposalTypeRepository.save(ADULT_CUSTODY_TYPE)
        disposalRepository.save(CUSTODIAL_SENTENCE)
        disposalTypeRepository.save(COMMUNITY_ORDER_TYPE)
        disposalRepository.save(COMMUNITY_SENTENCE)
        disposalRepository.save(CA_COMMUNITY_SENTENCE)
        disposalRepository.save(TERMINATION_COMMUNITY_SENTENCE)
        disposalRepository.save(TERMINATION_CUSTODIAL_SENTENCE)
        custodyRepository.save(CUSTODY)
        releaseRepository.save(RELEASE)
        contactTypeRepository.save(TWO_THIRDS_CONTACT_TYPE)
        contactTypeRepository.save(OTHER_CONTACT_TYPE)
        contactTypeRepository.save(APPOINTMENT_CONTACT_TYPE)
        contactTypeRepository.save(THREE_WAY_MEETING_TYPE)
        contactTypeRepository.save(REFER_TO_MANAGER_CONTACT_TYPE)
        contactTypeRepository.save(ENFORCEMENT_REVIEW_CONTACT_TYPE)
        contactTypeRepository.save(COMPONENT_TERMINATED_CONTACT_TYPE)
        contactTypeRepository.save(COMPONENT_TRANSFER_REJECTED_CONTACT_TYPE)
        contactTypeRepository.saveAll(STATUS_CONTACT_TYPES)
        contactOutcomeRepository.save(ATTENDED_COMPLIED)
        contactOutcomeRepository.save(FAILED_TO_COMPLY)
        enforcementActionRepository.save(REFER_TO_MANAGER_ACTION)
        referenceDataRepository.save(PSS_END_DATE_KEY_DATE_TYPE)
        keyDateRepository.save(PSS_END_DATE)
        referenceDataRepository.save(LED_KEY_DATE_TYPE)
        keyDateRepository.save(LED_DATE)
        pssRequirementMainCategoryRepository.save(PSS_MAIN_TYPE)
        pssRequirementSubCategoryRepository.save(PSS_SUB_TYPE)
        pssRequirementRepository.saveAll(PSS_REQUIREMENTS)
        licenceConditionMainCategoryRepository.save(LICENCE_CONDITION_MAIN_TYPE)
        referenceDataRepository.save(LICENCE_CONDITION_SUB_TYPE)
        licenceConditionRepository.saveAll(LICENCE_CONDITIONS)
        licenceConditionManagerRepository.saveAll(LICENCE_CONDITION_MANAGERS)
        requirementMainCategoryRepository.save(REQUIREMENT_MAIN_TYPE)
        referenceDataRepository.save(REQUIREMENT_SUB_TYPE)
        requirementRepository.saveAll(REQUIREMENTS)
        requirementManagerRepository.saveAll(REQUIREMENT_MANAGERS)
        requirementRepository.saveAll(TERMINATION_REQUIREMENTS)
        requirementManagerRepository.saveAll(TERMINATION_REQUIREMENT_MANAGERS)
        requirementTransferRepository.save(REQUIREMENT_TRANSFER)
        licenceConditionRepository.save(TERMINATION_LICENCE_CONDITION)
        licenceConditionManagerRepository.save(TERMINATION_LICENCE_CONDITION_MANAGER)
        eventRepository.flush()
        offenceRepository.saveAll(OFFENCES)
        mainOffenceRepository.save(MAIN_OFFENCE)
        additionalOffenceRepository.save(ADDITIONAL_OFFENCE)
        registerTypeRepository.save(REGISTER_TYPE)
        referenceDataRepository.save(REGISTER_CATEGORY)
        registrationRepository.save(REGISTRATION)
        contactRepository.save(TWO_THIRDS_CONTACT)
        contactRepository.save(OTHER_CONTACT)
        contactRepository.save(TERMINATION_CONTACT)
        contactRepository.saveAll(APPOINTMENTS)
    }
}