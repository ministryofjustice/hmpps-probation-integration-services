package uk.gov.justice.digital.hmpps.data

import UserGenerator
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonRepository
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceSubTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalContactGenerator
import uk.gov.justice.digital.hmpps.data.repository.AddressRepository
import uk.gov.justice.digital.hmpps.data.repository.PersonalCircumstanceRepository
import uk.gov.justice.digital.hmpps.data.repository.PersonalCircumstanceSubTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.PersonalCircumstanceTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.PersonalContactRepository
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val serviceContext: ServiceContext,
    private val userRepository: UserRepository,
    private val personRepository: PersonRepository,
    private val personalCircumstanceTypeRepository: PersonalCircumstanceTypeRepository,
    private val personalCircumstanceSubTypeRepository: PersonalCircumstanceSubTypeRepository,
    private val personalCircumstanceRepository: PersonalCircumstanceRepository,
    private val addressRepository: AddressRepository,
    private val personalContactRepository: PersonalContactRepository,

) : CommandLineRunner {
    @Transactional
    override fun run(vararg args: String?) {
        userRepository.save(UserGenerator.APPLICATION_USER)
        serviceContext.setUp()

        // Perform dev/test database setup here, using JPA repositories and generator classes...
        personalCircumstanceTypeRepository.save(PersonalCircumstanceTypeGenerator.DEFAULT)
        personalCircumstanceSubTypeRepository.save(PersonalCircumstanceSubTypeGenerator.DEFAULT)
        personRepository.save(PersonGenerator.DEFAULT)
        personalCircumstanceRepository.save(PersonalCircumstanceGenerator.DEFAULT)
        addressRepository.save(AddressGenerator.DEFAULT)
        personalContactRepository.save(PersonalContactGenerator.DEFAULT)
    }
}
