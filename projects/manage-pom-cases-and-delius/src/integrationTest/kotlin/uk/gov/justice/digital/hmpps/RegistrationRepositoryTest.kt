package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffRepository
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class RegistrationRepositoryTest {
    @Autowired
    lateinit var personRepository: PersonRepository

    @Autowired
    lateinit var registrationRepository: RegistrationRepository

    @Autowired
    lateinit var staffRepository: StaffRepository

    @Test
    fun `correctly identifies when a vlo is assigned`() {
        val person = givenPerson()
        val staff = givenStaff()
        hasQualifyingRegistration(person, staff)

        val vloAssigned = registrationRepository.hasVloAssigned(person.id)
        assertTrue(vloAssigned)
    }

    private fun givenPerson() =
        personRepository.save(PersonGenerator.generate("V123456", "V1234LO"))

    private fun givenStaff() =
        staffRepository.save(ProviderGenerator.generateStaff("TEST", "TEST", "TEST"))

    private fun hasQualifyingRegistration(person: Person, staff: Staff) =
        registrationRepository.save(
            RegistrationGenerator.generate(
                RegistrationGenerator.TYPE_DASO,
                null,
                LocalDate.now().minusDays(1),
                person = person,
                staff = staff
            )
        )
}
