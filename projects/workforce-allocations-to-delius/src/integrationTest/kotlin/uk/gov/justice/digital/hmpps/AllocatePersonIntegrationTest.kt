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
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ResponsibleOfficerGenerator
import uk.gov.justice.digital.hmpps.data.repository.IapsPersonRepository
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.EventType
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@SpringBootTest
@ActiveProfiles("integration-test")
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
        val person = PersonGenerator.NEW_PM
        val initialPm = PersonManagerGenerator.NEW
        val initialRo = ResponsibleOfficerGenerator.NEW

        allocateAndValidate(
            "new-person-allocation-message",
            "new-person-allocation-body",
            initialPm,
            initialRo,
            person,
            1,
            1
        )
    }

    @Test
    fun `allocate historic person manager`() {
        val person = PersonGenerator.HISTORIC_PM
        val initialPm = PersonManagerGenerator.HISTORIC
        val initialRo = ResponsibleOfficerGenerator.HISTORIC

        val firstPm = personManagerRepository.save(
            personManagerRepository.findByIdOrNull(initialPm.id)?.apply {
                endDate = ZonedDateTime.now().minusDays(1)
            }!!
        )
        val secondPm = personManagerRepository.save(
            PersonManagerGenerator.generate(
                personId = person.id,
                startDateTime = firstPm.endDate!!
            )
        )

        val firstRo = responsibleOfficerRepository.save(
            responsibleOfficerRepository.findByIdOrNull(initialRo.id)?.apply {
                endDate = ZonedDateTime.now().minusDays(1)
            }!!
        )
        val secondRo =
            responsibleOfficerRepository.save(
                ResponsibleOfficerGenerator.generate(
                    personId = person.id,
                    startDateTime = firstRo.endDate!!
                )
            )

        allocateAndValidate(
            "historic-person-allocation-message",
            "historic-person-allocation-body",
            firstPm,
            firstRo,
            person,
            2,
            2
        )

        val insertedPm = personManagerRepository.findActiveManagerAtDate(person.id, ZonedDateTime.now().minusDays(2))
        assertThat(
            insertedPm?.endDate?.truncatedTo(ChronoUnit.SECONDS)?.withZoneSameInstant(EuropeLondon),
            equalTo(secondPm.startDate.truncatedTo(ChronoUnit.SECONDS).withZoneSameInstant(EuropeLondon))
        )
        val insertedRo =
            responsibleOfficerRepository.findActiveManagerAtDate(person.id, ZonedDateTime.now().minusDays(2))
        assertThat(
            insertedRo?.endDate?.truncatedTo(ChronoUnit.SECONDS)?.withZoneSameInstant(EuropeLondon),
            equalTo(secondRo.startDate.truncatedTo(ChronoUnit.SECONDS).withZoneSameInstant(EuropeLondon))
        )
    }

    private fun allocateAndValidate(
        messageName: String,
        jsonFile: String,
        existingPm: PersonManager,
        existingRo: ResponsibleOfficer,
        person: Person,
        originalPmCount: Int,
        originalRoCount: Int
    ) {
        val allocationEvent = prepMessage(messageName, wireMockServer.port())
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

        val allocationDetail = ResourceLoader.allocationBody(jsonFile)

        val oldPm = personManagerRepository.findById(existingPm.id).orElseThrow()
        assertThat(
            oldPm.endDate?.withZoneSameInstant(EuropeLondon),
            equalTo(allocationDetail.createdDate.withZoneSameInstant(EuropeLondon))
        )

        val oldRo = responsibleOfficerRepository.findById(existingRo.id).orElseThrow()
        assertThat(
            oldRo.endDate?.withZoneSameInstant(EuropeLondon),
            equalTo(allocationDetail.createdDate.withZoneSameInstant(EuropeLondon))
        )

        val updatedPmCount = personManagerRepository.findAll().count { it.personId == person.id }
        assertThat(updatedPmCount, equalTo(originalPmCount + 1))

        val updatedRoCount = responsibleOfficerRepository.findAll().count { it.personId == person.id }
        assertThat(updatedRoCount, equalTo(originalRoCount + 1))

        assert(iapsPersonRepository.findById(person.id).isPresent)
    }
}
