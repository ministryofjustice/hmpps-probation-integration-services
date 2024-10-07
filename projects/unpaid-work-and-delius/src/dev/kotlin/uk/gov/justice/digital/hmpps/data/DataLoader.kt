package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CaseRepository
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.EventRepository
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.repository.*
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.generateExclusion
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.generateRestriction
import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.integrations.common.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.person.PersonWithManagerRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.team.TeamRepository
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val caseRepository: CaseRepository,
    private val datasetRepository: DatasetRepository,
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val personalCircumstanceTypeRepository: PersonalCircumstanceTypeRepository,
    private val personalCircumstanceSubTypeRepository: PersonalCircumstanceSubTypeRepository,
    private val personalCircumstanceRepository: PersonalCircumstanceRepository,
    private val addressRepository: AddressRepository,
    private val aliasRepository: AliasRepository,
    private val caseAddressRepository: CaseAddressRepository,
    private val disabilityRepository: DisabilityRepository,
    private val provisionRepository: ProvisionRepository,
    private val registrationRepository: RegistrationRepository,
    private val registerTypeRepository: RegisterTypeRepository,
    private val offenceRepository: OffenceRepository,
    private val eventRepository: EventRepository,
    private val disposalRepository: DisposalRepository,
    private val mainOffenceRepository: MainOffenceRepository,
    private val personalContactRepository: PersonalContactRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val personWithManagerRepository: PersonWithManagerRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val exclusionRepository: ExclusionRepository,
    private val restrictionRepository: RestrictionRepository

) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
        auditUserRepository.save(UserGenerator.LIMITED_ACCESS_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        datasetRepository.saveAll(
            listOf(
                DatasetGenerator.GENDER,
                DatasetGenerator.ETHNICITY,
                DatasetGenerator.DISABILITY,
                DatasetGenerator.DISABILITY_CONDITION,
                DatasetGenerator.LANGUAGE,
                DatasetGenerator.REGISTER_LEVEL,
                DatasetGenerator.REGISTER_CATEGORY,
                DatasetGenerator.DISABILITY_PROVISION,
                DatasetGenerator.DISABILITY_PROVISION_CATEGORY,
                DatasetGenerator.RELATIONSHIP,
                DatasetGenerator.ADDRESS_STATUS
            )
        )
        businessInteractionRepository.saveAll(
            listOf(BusinessInteractionGenerator.UPLOAD_DOCUMENT)
        )

        referenceDataRepository.saveAll(
            listOf(
                ReferenceDataGenerator.GENDER_MALE,
                ReferenceDataGenerator.ETHNICITY_INDIAN,
                ReferenceDataGenerator.DISABILITY_HEARING,
                ReferenceDataGenerator.DISABILITY_HEARING_CONDITION,
                ReferenceDataGenerator.LANGUAGE_ENGLISH,
                ReferenceDataGenerator.MAPPA_LEVEL_1,
                ReferenceDataGenerator.MAPPA_CATEGORY_2,
                ReferenceDataGenerator.HEARING_PROVISION,
                ReferenceDataGenerator.HEARING_PROVISION_CATEGORY,
                ReferenceDataGenerator.DOCTOR_RELATIONSHIP,
                ReferenceDataGenerator.MAIN_ADDRESS

            )
        )
        contactTypeRepository.save(ContactTypeGenerator.DEFAULT)
        staffRepository.save(StaffGenerator.DEFAULT)
        teamRepository.save(TeamGenerator.DEFAULT)
        offenceRepository.save(OffenceGenerator.DEFAULT)
        personalCircumstanceTypeRepository.save(PersonalCircumstanceTypeGenerator.DEFAULT)
        personalCircumstanceSubTypeRepository.save(PersonalCircumstanceSubTypeGenerator.DEFAULT)
        caseRepository.saveAndFlush(CaseGenerator.DEFAULT)
        caseRepository.saveAndFlush(CaseGenerator.EXCLUSION)
        caseRepository.saveAndFlush(CaseGenerator.RESTRICTION)
        caseRepository.saveAndFlush(CaseGenerator.RESTRICTION_EXCLUSION)
        aliasRepository.save(AliasGenerator.DEFAULT)
        personalCircumstanceRepository.save(PersonalCircumstanceGenerator.DEFAULT)
        addressRepository.save(AddressGenerator.DEFAULT)
        personalContactRepository.save(PersonalContactGenerator.DEFAULT)
        caseAddressRepository.save(CaseAddressGenerator.DEFAULT)
        disabilityRepository.save(DisabilityGenerator.DEFAULT)
        provisionRepository.save(ProvisionGenerator.DEFAULT)
        registerTypeRepository.save(RegisterTypeGenerator.DEFAULT)
        registrationRepository.save(RegistrationGenerator.DEFAULT)
        eventRepository.save(EventGenerator.DEFAULT)
        disposalRepository.save(DisposalGenerator.DEFAULT)
        mainOffenceRepository.save(MainOffenceGenerator.DEFAULT)
        personManagerRepository.save(PersonManagerGenerator.DEFAULT)

        exclusionRepository.save(LimitedAccessGenerator.EXCLUSION)
        restrictionRepository.save(LimitedAccessGenerator.RESTRICTION)
        exclusionRepository.save(generateExclusion(person = CaseGenerator.RESTRICTION_EXCLUSION))
        restrictionRepository.save(generateRestriction(person = CaseGenerator.RESTRICTION_EXCLUSION))
    }
}

interface RestrictionRepository : JpaRepository<Restriction, Long>
interface ExclusionRepository : JpaRepository<Exclusion, Long>