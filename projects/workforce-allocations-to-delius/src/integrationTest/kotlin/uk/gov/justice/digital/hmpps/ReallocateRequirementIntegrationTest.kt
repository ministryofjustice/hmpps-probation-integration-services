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
import uk.gov.justice.digital.hmpps.data.generator.RequirementGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementManagerGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManagerRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
class ReallocateRequirementIntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}")
    private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val wireMockServer: WireMockServer,
    private val requirementManagerRepository: RequirementManagerRepository,
) {
    @MockitoBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `reallocate new requirement manager`() {
        val requirement = RequirementGenerator.REALLOCATION
        val existingManager = RequirementManagerGenerator.REALLOCATION

        allocateAndValidate(
            "new-requirement-reallocation-message",
            "reallocation-requirement-allocation-body",
            existingManager,
        )

        verify(telemetryService).trackEvent(
            eq("RequirementAllocation"),
            eq(
                mapOf(
                    "crn" to requirement.person.crn,
                    "detailUrl" to "http://localhost:${wireMockServer.port()}/allocation/requirements/reallocate-new-requirement-manager"
                )
            ),
            any()
        )
    }

    private fun allocateAndValidate(
        messageName: String,
        jsonFile: String,
        existingRm: RequirementManager,
    ) {
        val allocationEvent = prepMessage(messageName, wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(allocationEvent)

        val allocationDetail = ResourceLoader.file<AllocationDetail>(jsonFile)

        val expectedAllocationReason =
            deriveDeliusCodeFromTextDefaultInitial(
                allocationDetail.allocationReason,
                AllocationType.REQUIREMENT
            )

        assertThat(
            expectedAllocationReason,
            equalTo(requirementManagerRepository.findById(existingRm.id).get().allocationReason.code)
        )
    }
}
