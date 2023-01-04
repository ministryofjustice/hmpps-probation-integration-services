package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ResponsibleOfficerGenerator
import uk.gov.justice.digital.hmpps.data.repository.IapsPersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.time.ZonedDateTime

@SpringBootTest
@ActiveProfiles("integration-test")
class AllocatePersonIntegrationTest {

    @Value("\${messaging.consumer.queue}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var channelManager: HmppsChannelManager

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

        val insertedPm = personManagerRepository.findActiveManager(person.id, ZonedDateTime.now().minusDays(2))
        assert(secondPm.startDate.closeTo(insertedPm?.endDate))

        val insertedRo =
            responsibleOfficerRepository.findActiveManagerAtDate(person.id, ZonedDateTime.now().minusDays(2))
        assert(secondRo.startDate.closeTo(insertedRo?.endDate))
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
        channelManager.getChannel(queueName).publishAndWait(allocationEvent)

        verify(telemetryService).notificationReceived(allocationEvent)

        val allocationDetail = ResourceLoader.file<AllocationDetail>(jsonFile)

        val oldPm = personManagerRepository.findById(existingPm.id).orElseThrow()
        assert(allocationDetail.createdDate.closeTo(oldPm.endDate))

        val oldRo = responsibleOfficerRepository.findById(existingRo.id).orElseThrow()
        assert(allocationDetail.createdDate.closeTo(oldRo.endDate))

        val updatedPmCount = personManagerRepository.findAll().count { it.personId == person.id }
        assertThat(updatedPmCount, equalTo(originalPmCount + 1))

        val updatedRoCount = responsibleOfficerRepository.findAll().count { it.personId == person.id }
        assertThat(updatedRoCount, equalTo(originalRoCount + 1))

        assert(iapsPersonRepository.findById(person.id).isPresent)
    }
}
