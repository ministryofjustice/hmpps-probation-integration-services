package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@SpringBootTest
class AllocateEventIntegrationTest {

    @Value("\${messaging.consumer.queue}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var channelManager: HmppsChannelManager

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired
    private lateinit var orderManagerRepository: OrderManagerRepository

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Autowired
    private lateinit var contactRepository: ContactRepository

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

        verify(telemetryService).trackEvent(
            eq("EventAllocation"),
            eq(
                mapOf(
                    "crn" to event.person.crn,
                    "detailUrl" to "http://localhost:${wireMockServer.port()}/allocation/event/allocate-new-order-manager"
                )
            ),
            any()
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
        assert(secondOm.startDate.closeTo(insertedPm?.endDate))

        verify(telemetryService).trackEvent(
            eq("EventAllocation"),
            eq(
                mapOf(
                    "crn" to event.person.crn,
                    "detailUrl" to "http://localhost:${wireMockServer.port()}/allocation/event/allocate-historic-order-manager"
                )
            ),
            any()
        )
    }

    private fun allocateAndValidate(
        messageName: String,
        jsonFile: String,
        existingOm: OrderManager,
        event: Event,
        originalOmCount: Int
    ) {
        val allocationEvent = prepMessage(messageName, wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(allocationEvent)

        val allocationDetail = ResourceLoader.file<AllocationDetail>(jsonFile)

        val oldOm = orderManagerRepository.findById(existingOm.id).orElseThrow()
        assert(allocationDetail.createdDate.closeTo(oldOm.endDate))

        val updatedOmCount = orderManagerRepository.findAll().count { it.eventId == event.id }
        assertThat(updatedOmCount, equalTo(originalOmCount + 1))

        val cadeContact = contactRepository.findAll()
            .firstOrNull { it.eventId == oldOm.eventId && it.type.code == ContactTypeCode.CASE_ALLOCATION_DECISION_EVIDENCE.value }

        assertNotNull(cadeContact)

        assertThat(
            cadeContact!!.isSensitive,
            equalTo((allocationDetail as AllocationDetail.EventAllocation).sensitive)
        )
        assertThat(cadeContact.notes, equalTo(allocationDetail.notes))
    }
}
