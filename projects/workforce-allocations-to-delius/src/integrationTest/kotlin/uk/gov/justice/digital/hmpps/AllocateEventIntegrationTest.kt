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
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.repository.IapsEventRepository
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.EventType
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@ActiveProfiles("integration-test")
@SpringBootTest
class AllocateEventIntegrationTest {

    @Value("\${spring.jms.template.default-destination}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired
    private lateinit var orderManagerRepository: OrderManagerRepository

    @Autowired
    private lateinit var iapsEventRepository: IapsEventRepository

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `allocate new order manager`() {
        val event = EventGenerator.NEW
        val existingManager = OrderManagerGenerator.NEW

        allocateAndValidate(
            "new-event-allocation-message",
            "new-event-allocation-body",
            existingManager,
            event,
            1
        )
    }

    @Test
    fun `allocate historic order manager`() {
        val event = EventGenerator.HISTORIC

        val firstOm = orderManagerRepository.save(
            orderManagerRepository.findByIdOrNull(OrderManagerGenerator.HISTORIC.id)?.apply {
                endDate = ZonedDateTime.now().minusDays(1)
            }!!
        )
        val secondOm = orderManagerRepository.save(
            OrderManagerGenerator.generate(
                eventId = event.id,
                startDateTime = firstOm.endDate!!
            )
        )

        allocateAndValidate(
            "historic-event-allocation-message",
            "historic-event-allocation-body",
            firstOm,
            event,
            2
        )

        val insertedPm = orderManagerRepository.findActiveManagerAtDate(event.id, ZonedDateTime.now().minusDays(2))
        assertThat(
            insertedPm?.endDate?.truncatedTo(ChronoUnit.SECONDS)?.withZoneSameInstant(EuropeLondon),
            equalTo(secondOm.startDate.truncatedTo(ChronoUnit.SECONDS).withZoneSameInstant(EuropeLondon))
        )
    }

    private fun allocateAndValidate(
        messageName: String,
        jsonFile: String,
        existingOm: OrderManager,
        event: Event,
        originalOmCount: Int,
    ) {
        val allocationEvent = prepMessage(messageName, wireMockServer.port())
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

        val allocationDetail = ResourceLoader.allocationBody(jsonFile)

        val oldOm = orderManagerRepository.findById(existingOm.id).orElseThrow()
        assertThat(
            oldOm.endDate?.withZoneSameInstant(EuropeLondon),
            equalTo(allocationDetail.createdDate.withZoneSameInstant(EuropeLondon))
        )

        val updatedOmCount = orderManagerRepository.findAll().count { it.eventId == event.id }
        assertThat(updatedOmCount, equalTo(originalOmCount + 1))

        assert(iapsEventRepository.findById(event.id).isPresent)
    }
}
