package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.api.model.AllocationType
import uk.gov.justice.digital.hmpps.api.model.deriveDeliusCodeDefaultInitial
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
class ReallocateEventIntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}") private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val wireMockServer: WireMockServer,
    private val orderManagerRepository: OrderManagerRepository,
    private val contactRepository: ContactRepository
) {
    @MockitoBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `reallocation completes and has correct allocation reason`() {
        val event = EventGenerator.REALLOCATION
        val existingManager = OrderManagerGenerator.REALLOCATION

        allocateAndValidate(
            "new-event-reallocation-message",
            "reallocation-event-allocation-body",
            existingManager,
        )

        verify(telemetryService).trackEvent(
            eq("EventAllocation"),
            eq(
                mapOf(
                    "crn" to event.person.crn,
                    "detailUrl" to "http://localhost:${wireMockServer.port()}/allocation/event/reallocate-new-order-manager"
                )
            ),
            any()
        )
    }

    private fun allocateAndValidate(
        messageName: String,
        jsonFile: String,
        existingOm: OrderManager,
    ) {
        val allocationEvent = prepMessage(messageName, wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(allocationEvent)

        val allocationDetail = ResourceLoader.file<AllocationDetail>(jsonFile)

        val expectedAllocationReason =
            deriveDeliusCodeDefaultInitial(allocationDetail.allocationReason, AllocationType.ORDER)

        assertThat(
            orderManagerRepository.findById(existingOm.id).get().allocationReason.code,
            equalTo(expectedAllocationReason)
        )
    }
}
