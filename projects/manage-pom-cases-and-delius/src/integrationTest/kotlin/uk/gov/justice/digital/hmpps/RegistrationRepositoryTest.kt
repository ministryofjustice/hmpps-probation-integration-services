package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegistrationRepository
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class RegistrationRepositoryTest {
    @Autowired
    lateinit var personRepository: PersonRepository

    @Autowired
    lateinit var registrationRepository: RegistrationRepository

    @Test
    fun `correctly identifies when a vlo is assigned`() {
        val person = givenPerson()
        hasQualifyingRegistration(person)

        val vloAssigned = registrationRepository.hasVloAssigned(person.id)
        assertTrue(vloAssigned)
    }

    private fun givenPerson() =
        personRepository.save(PersonGenerator.generate("V123456", "V1234LO"))

    private fun hasQualifyingRegistration(person: Person) =
        registrationRepository.save(
            RegistrationGenerator.generate(
                RegistrationGenerator.TYPE_DASO,
                null,
                LocalDate.now().minusDays(1),
                person = person
            )
        )
}
