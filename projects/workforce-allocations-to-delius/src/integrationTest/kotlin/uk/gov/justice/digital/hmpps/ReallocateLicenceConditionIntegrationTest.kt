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
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionManagerGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionManagerRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
class ReallocateLicenceConditionIntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}")
    private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val wireMockServer: WireMockServer,
    private val licenceConditionManagerRepository: LicenceConditionManagerRepository,
) {
    @MockitoBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `reallocate new licence condition manager`() {
        val licenceCondition = LicenceConditionGenerator.REALLOCATION
        val existingManager = LicenceConditionManagerGenerator.REALLOCATION

        allocateAndValidate(
            "new-licence-condition-reallocation-message",
            "reallocation-licence-condition-allocation-body",
            existingManager,
        )

        verify(telemetryService).trackEvent(
            eq("LicenceConditionAllocation"),
            eq(
                mapOf(
                    "crn" to licenceCondition.person.crn,
                    "detailUrl" to "http://localhost:${wireMockServer.port()}/allocation/licence-conditions/reallocate-new-licence-condition-manager"
                )
            ),
            any()
        )
    }

    private fun allocateAndValidate(
        messageName: String,
        jsonFile: String,
        existingLcm: LicenceConditionManager,
    ) {
        val allocationEvent = prepMessage(messageName, wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(allocationEvent)

        val allocationDetail = ResourceLoader.file<AllocationDetail.LicenceConditionAllocation>(jsonFile)

        val expectedAllocationReason =
            deriveDeliusCodeDefaultInitial(allocationDetail.allocationReason, AllocationType.LICENCE_CONDITION)

        assertThat(
            expectedAllocationReason,
            equalTo(licenceConditionManagerRepository.findById(existingLcm.id).get().allocationReason.code)
        )
    }
}

