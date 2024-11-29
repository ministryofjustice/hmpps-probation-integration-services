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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.generator.RequirementGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementManagerGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManagerRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@SpringBootTest
class AllocateRequirementIntegrationTest {

    @Value("\${messaging.consumer.queue}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var channelManager: HmppsChannelManager

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired
    private lateinit var requirementManagerRepository: RequirementManagerRepository

    @MockitoBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `allocate new requirement manager`() {
        val requirement = RequirementGenerator.NEW
        val existingManager = RequirementManagerGenerator.NEW

        allocateAndValidate(
            "new-requirement-allocation-message",
            "new-requirement-allocation-body",
            existingManager,
            requirement,
            1
        )

        verify(telemetryService).trackEvent(
            eq("RequirementAllocation"),
            eq(
                mapOf(
                    "crn" to requirement.person.crn,
                    "detailUrl" to "http://localhost:${wireMockServer.port()}/allocation/requirements/allocate-new-requirement-manager"
                )
            ),
            any()
        )
    }

    @Test
    fun `allocate historic requirement manager`() {
        val requirement = RequirementGenerator.HISTORIC

        val firstRm = requirementManagerRepository.save(
            requirementManagerRepository.findByIdOrNull(RequirementManagerGenerator.HISTORIC.id)?.apply {
                endDate = ZonedDateTime.now().minusDays(1)
            }!!
        )
        val secondRm =
            requirementManagerRepository.save(
                RequirementManagerGenerator.generate(
                    requirementId = requirement.id,
                    startDateTime = firstRm.endDate!!
                )
            )

        allocateAndValidate(
            "historic-requirement-allocation-message",
            "historic-requirement-allocation-body",
            firstRm,
            requirement,
            2
        )

        val insertedRm = requirementManagerRepository.findActiveManagerAtDate(
            requirement.id,
            ZonedDateTime.now().minusDays(2)
        )
        assert(secondRm.startDate.closeTo(insertedRm?.endDate))

        verify(telemetryService).trackEvent(
            eq("RequirementAllocation"),
            eq(
                mapOf(
                    "crn" to requirement.person.crn,
                    "detailUrl" to "http://localhost:${wireMockServer.port()}/allocation/requirements/allocate-historic-requirement-manager"
                )
            ),
            any()
        )
    }

    private fun allocateAndValidate(
        messageName: String,
        jsonFile: String,
        existingRm: RequirementManager,
        requirement: Requirement,
        originalRmCount: Int
    ) {
        val allocationEvent = prepMessage(messageName, wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(allocationEvent)

        val allocationDetail = ResourceLoader.file<AllocationDetail>(jsonFile)

        val updatedRmCount = requirementManagerRepository.findAll().count { it.requirementId == requirement.id }
        assertThat(updatedRmCount, equalTo(originalRmCount + 1))

        val oldRm = requirementManagerRepository.findById(existingRm.id).orElseThrow()
        assert(allocationDetail.createdDate.closeTo(oldRm.endDate))
    }
}
