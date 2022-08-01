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
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.EventType
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@ActiveProfiles("integration-test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AllocateEventIntegrationTest {

    @Value("\${spring.jms.template.default-destination}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired
    private lateinit var orderManagerRepository: OrderManagerRepository

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `allocate new order manager`() {
        val event = EventGenerator.DEFAULT
        val existingManager = orderManagerRepository.findByIdOrNull(OrderManagerGenerator.DEFAULT.id)
            ?: throw NotFoundException("Order Manager Not Found")
        val originalOmCount = orderManagerRepository.findAll().count { it.eventId == event.id }

        allocateAndValidate(existingManager, event, originalOmCount)
    }

    @Test
    fun `allocate historic order manager`() {
        val event = EventGenerator.DEFAULT

        val firstOm = orderManagerRepository.save(
            orderManagerRepository.findByIdOrNull(OrderManagerGenerator.DEFAULT.id)?.apply {
                endDate = ZonedDateTime.now().minusDays(1)
            }!!
        )
        val secondOm = orderManagerRepository.save(OrderManagerGenerator.generate(startDateTime = firstOm.endDate!!))

        val originalOmCount = orderManagerRepository.findAll().count { it.eventId == event.id }

        allocateAndValidate(firstOm, event, originalOmCount)

        val insertedPm = orderManagerRepository.findActiveManagerAtDate(event.id, ZonedDateTime.now().minusDays(2))
        assertThat(
            insertedPm?.endDate?.truncatedTo(ChronoUnit.SECONDS),
            equalTo(secondOm.startDate.truncatedTo(ChronoUnit.SECONDS))
        )
    }

    private fun allocateAndValidate(
        existingOm: OrderManager,
        event: Event,
        originalOmCount: Int,
    ) {
        val allocationEvent = prepMessage("event-allocation-message", wireMockServer.port())
        jmsTemplate.convertSendAndWait(queueName, allocationEvent)

        verify(telemetryService).trackEvent(
            eq("${EventType.EVENT_ALLOCATED}_RECEIVED"),
            eq(
                mapOf(
                    "eventType" to allocationEvent.eventType.value,
                    "detailUrl" to allocationEvent.detailUrl,
                    "CRN" to allocationEvent.personReference.findCrn()!!
                )
            ),
            ArgumentMatchers.anyMap()
        )

        val allocationDetail = ResourceLoader.allocationBody("get-event-allocation-body")

        val oldOm = orderManagerRepository.findById(existingOm.id).orElseThrow()
        assertThat(oldOm.endDate, equalTo(allocationDetail.createdDate))

        val updatedOmCount = orderManagerRepository.findAll().count { it.eventId == event.id }
        assertThat(originalOmCount + 1, equalTo(updatedOmCount))
    }
}
