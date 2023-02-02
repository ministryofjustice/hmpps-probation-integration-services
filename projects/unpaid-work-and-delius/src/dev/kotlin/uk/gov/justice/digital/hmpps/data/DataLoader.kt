package uk.gov.justice.digital.hmpps.data

import UserGenerator
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CaseRepository
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.EventRepository
import uk.gov.justice.digital.hmpps.controller.common.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.AliasGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseAddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseGenerator
import uk.gov.justice.digital.hmpps.data.generator.DatasetGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisabilityGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisposalGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.MainOffenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceSubTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProvisionGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegisterTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.data.repository.AddressRepository
import uk.gov.justice.digital.hmpps.data.repository.AliasRepository
import uk.gov.justice.digital.hmpps.data.repository.CaseAddressRepository
import uk.gov.justice.digital.hmpps.data.repository.DatasetRepository
import uk.gov.justice.digital.hmpps.data.repository.DisabilityRepository
import uk.gov.justice.digital.hmpps.data.repository.DisposalRepository
import uk.gov.justice.digital.hmpps.data.repository.MainOffenceRepository
import uk.gov.justice.digital.hmpps.data.repository.OffenceRepository
import uk.gov.justice.digital.hmpps.data.repository.PersonalCircumstanceRepository
import uk.gov.justice.digital.hmpps.data.repository.PersonalCircumstanceSubTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.PersonalCircumstanceTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.PersonalContactRepository
import uk.gov.justice.digital.hmpps.data.repository.ProvisionRepository
import uk.gov.justice.digital.hmpps.data.repository.RegisterTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.RegistrationRepository
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val serviceContext: ServiceContext,
    private val userRepository: UserRepository,
    private val caseRepository: CaseRepository,
    private val datasetRepository: DatasetRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val personalCircumstanceTypeRepository: PersonalCircumstanceTypeRepository,
    private val personalCircumstanceSubTypeRepository: PersonalCircumstanceSubTypeRepository,
    private val personalCircumstanceRepository: PersonalCircumstanceRepository,
    private val addressRepository: AddressRepository,
    private val aliasRepository: AliasRepository,
    private val personalContactRepository: PersonalContactRepository,
    private val caseAddressRepository: CaseAddressRepository,
    private val disabilityRepository: DisabilityRepository,
    private val provisionRepository: ProvisionRepository,
    private val registrationRepository: RegistrationRepository,
    private val registerTypeRepository: RegisterTypeRepository,
    private val offenceRepository: OffenceRepository,
    private val eventRepository: EventRepository,
    private val disposalRepository: DisposalRepository,
    private val mainOffenceRepository: MainOffenceRepository

) : CommandLineRunner {
    @Transactional
    override fun run(vararg args: String?) {
        userRepository.save(UserGenerator.APPLICATION_USER)
        serviceContext.setUp()

        datasetRepository.saveAll(
            listOf(
                DatasetGenerator.GENDER,
                DatasetGenerator.ETHNICITY,
                DatasetGenerator.DISABILITY,
                DatasetGenerator.LANGUAGE,
                DatasetGenerator.REGISTER_LEVEL,
                DatasetGenerator.REGISTER_CATEGORY,
                DatasetGenerator.DISABILITY_PROVISION
            )
        )

        referenceDataRepository.saveAll(
            listOf(
                ReferenceDataGenerator.GENDER_MALE,
                ReferenceDataGenerator.ETHNICITY_INDIAN,
                ReferenceDataGenerator.DISABILITY_HEARING,
                ReferenceDataGenerator.LANGUAGE_ENGLISH,
                ReferenceDataGenerator.MAPPA_LEVEL_1,
                ReferenceDataGenerator.MAPPA_CATEGORY_2,
                ReferenceDataGenerator.HEARING_PROVISION
            )
        )
        offenceRepository.save(OffenceGenerator.DEFAULT)
        // Perform dev/test database setup here, using JPA repositories and generator classes...
        personalCircumstanceTypeRepository.save(PersonalCircumstanceTypeGenerator.DEFAULT)
        personalCircumstanceSubTypeRepository.save(PersonalCircumstanceSubTypeGenerator.DEFAULT)
        caseRepository.save(CaseGenerator.DEFAULT)
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
    }
}
