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
import uk.gov.justice.digital.hmpps.api.model.deriveDeliusCodeFromTextDefaultInitial
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
class ReallocatePersonIntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}") private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val wireMockServer: WireMockServer,
    private val personManagerRepository: PersonManagerRepository,
) {
    @MockitoBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `reallocate person manager`() {
        val person = PersonGenerator.NEW_PM

        allocateAndValidate(
            "new-person-reallocation-message",
            "reallocation-person-allocation-body",
        )

        verify(telemetryService).trackEvent(
            eq("PersonAllocation"),
            eq(
                mapOf(
                    "crn" to person.crn,
                    "detailUrl" to "http://localhost:${wireMockServer.port()}/allocation/person/reallocate-new-person-manager"
                )
            ),
            any()
        )
    }

    private fun allocateAndValidate(
        messageName: String,
        jsonFile: String,
    ) {
        val allocationEvent = prepMessage(messageName, wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(allocationEvent)

        val allocationDetail = ResourceLoader.file<AllocationDetail>(jsonFile)

        val pm = personManagerRepository.findPersonManagersByStaffCode(allocationDetail.staffCode).get(0)

        val expectedAllocationReason = deriveDeliusCodeFromTextDefaultInitial(
            allocationDetail.allocationReason,
            AllocationType.PERSON
        )
        assertThat(pm.allocationReason.code, equalTo(expectedAllocationReason))
    }
}
