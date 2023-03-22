package uk.gov.justice.digital.hmpps.data

import UserGenerator
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CaseRepository
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.EventRepository
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.AliasGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseAddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisabilityGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisposalGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.MainOffenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceSubTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProvisionGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegisterTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.repository.AddressRepository
import uk.gov.justice.digital.hmpps.data.repository.AliasRepository
import uk.gov.justice.digital.hmpps.data.repository.CaseAddressRepository
import uk.gov.justice.digital.hmpps.data.repository.DatasetRepository
import uk.gov.justice.digital.hmpps.data.repository.DisabilityRepository
import uk.gov.justice.digital.hmpps.data.repository.DisposalRepository
import uk.gov.justice.digital.hmpps.data.repository.MainOffenceRepository
import uk.gov.justice.digital.hmpps.data.repository.OffenceRepository
import uk.gov.justice.digital.hmpps.data.repository.PersonManagerRepository
import uk.gov.justice.digital.hmpps.data.repository.PersonalCircumstanceRepository
import uk.gov.justice.digital.hmpps.data.repository.PersonalCircumstanceSubTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.PersonalCircumstanceTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.PersonalContactRepository
import uk.gov.justice.digital.hmpps.data.repository.ProvisionRepository
import uk.gov.justice.digital.hmpps.data.repository.RegisterTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.person.PersonWithManagerRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.team.TeamRepository
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val userRepository: UserRepository,
    private val caseRepository: CaseRepository,
    private val datasetRepository: DatasetRepository,
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
    private val contactTypeRepository: ContactTypeRepository

) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
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
                DatasetGenerator.RELATIONSHIP
            )
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
                ReferenceDataGenerator.DOCTOR_RELATIONSHIP

            )
        )
        contactTypeRepository.save(ContactTypeGenerator.DEFAULT)
        staffRepository.save(StaffGenerator.DEFAULT)
        teamRepository.save(TeamGenerator.DEFAULT)
        offenceRepository.save(OffenceGenerator.DEFAULT)
        personalCircumstanceTypeRepository.save(PersonalCircumstanceTypeGenerator.DEFAULT)
        personalCircumstanceSubTypeRepository.save(PersonalCircumstanceSubTypeGenerator.DEFAULT)
        caseRepository.saveAndFlush(CaseGenerator.DEFAULT)
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
    }
}
