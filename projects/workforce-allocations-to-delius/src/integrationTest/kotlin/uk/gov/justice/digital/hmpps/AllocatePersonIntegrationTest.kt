package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ResponsibleOfficerGenerator
import uk.gov.justice.digital.hmpps.data.repository.IapsPersonRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.EventType
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@ActiveProfiles("integration-test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AllocatePersonIntegrationTest {

    @Value("\${spring.jms.template.default-destination}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired
    private lateinit var personManagerRepository: PersonManagerRepository

    @Autowired
    private lateinit var responsibleOfficerRepository: ResponsibleOfficerRepository

    @Autowired
    private lateinit var iapsPersonRepository: IapsPersonRepository

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `allocate new person manager`() {
        val person = PersonGenerator.DEFAULT
        val existingPm = personManagerRepository.findByIdOrNull(PersonManagerGenerator.DEFAULT.id)
            ?: throw NotFoundException("PM Not Found")
        val existingRo = responsibleOfficerRepository.findByIdOrNull(ResponsibleOfficerGenerator.DEFAULT.id)
            ?: throw NotFoundException("RO Not Found")
        val originalPmCount = personManagerRepository.findAll().count { it.personId == person.id }
        val originalRoCount = responsibleOfficerRepository.findAll().count { it.personId == person.id }

        allocateAndValidate(existingPm, existingRo, person, originalPmCount, originalRoCount)
    }

    @Test
    fun `allocate historic person manager`() {
        val person = PersonGenerator.DEFAULT

        val firstPm = personManagerRepository.save(
            personManagerRepository.findByIdOrNull(PersonManagerGenerator.DEFAULT.id)?.apply {
                endDate = ZonedDateTime.now().minusDays(1)
            }!!
        )
        val secondPm = personManagerRepository.save(PersonManagerGenerator.generate(startDateTime = firstPm.endDate!!))

        val firstRo = responsibleOfficerRepository.save(
            responsibleOfficerRepository.findByIdOrNull(ResponsibleOfficerGenerator.DEFAULT.id)?.apply {
                endDate = ZonedDateTime.now().minusDays(1)
            }!!
        )
        val secondRo =
            responsibleOfficerRepository.save(ResponsibleOfficerGenerator.generate(startDateTime = firstRo.endDate!!))

        val originalPmCount = personManagerRepository.findAll().count { it.personId == person.id }
        val originalRoCount = responsibleOfficerRepository.findAll().count { it.personId == person.id }

        allocateAndValidate(firstPm, firstRo, person, originalPmCount, originalRoCount)

        val insertedPm = personManagerRepository.findActiveManagerAtDate(person.id, ZonedDateTime.now().minusDays(2))
        assertThat(insertedPm?.endDate?.truncatedTo(ChronoUnit.SECONDS), equalTo(secondPm.startDate.truncatedTo(ChronoUnit.SECONDS)))
        val insertedRo =
            responsibleOfficerRepository.findActiveManagerAtDate(person.id, ZonedDateTime.now().minusDays(2))
        assertThat(insertedRo?.endDate?.truncatedTo(ChronoUnit.SECONDS), equalTo(secondRo.startDate.truncatedTo(ChronoUnit.SECONDS)))
    }

    private fun allocateAndValidate(
        existingPm: PersonManager,
        existingRo: ResponsibleOfficer,
        person: Person,
        originalPmCount: Int,
        originalRoCount: Int
    ) {
        val allocationEvent = prepMessage("person-allocation-message", wireMockServer.port())
        jmsTemplate.convertSendAndWait(queueName, allocationEvent)

        verify(telemetryService).trackEvent(
            eq("${EventType.PERSON_ALLOCATED}_RECEIVED"),
            eq(
                mapOf(
                    "eventType" to allocationEvent.eventType.value,
                    "detailUrl" to allocationEvent.detailUrl,
                    "CRN" to allocationEvent.personReference.findCrn()!!
                )
            ),
            ArgumentMatchers.anyMap()
        )

        val allocationDetail = ResourceLoader.allocationBody("get-person-allocation-body")

        val oldPm = personManagerRepository.findById(existingPm.id).orElseThrow()
        assertThat(oldPm.endDate, equalTo(allocationDetail.createdDate))

        val oldRo = responsibleOfficerRepository.findById(existingRo.id).orElseThrow()
        assertThat(oldRo.endDate, equalTo(allocationDetail.createdDate))

        val updatedPmCount = personManagerRepository.findAll().count { it.personId == person.id }
        assertThat(originalPmCount + 1, equalTo(updatedPmCount))

        val updatedRoCount = responsibleOfficerRepository.findAll().count { it.personId == person.id }
        assertThat(originalRoCount + 1, equalTo(updatedRoCount))

        assert(iapsPersonRepository.findById(person.id).isPresent)
    }
}
