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
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionManagerGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionManagerRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@SpringBootTest
class AllocateLicenceConditionIntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}")
    private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val wireMockServer: WireMockServer,
    private val licenceConditionManagerRepository: LicenceConditionManagerRepository
) {
    @MockitoBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `allocate new licence condition manager`() {
        val licenceCondition = LicenceConditionGenerator.NEW
        val existingManager = LicenceConditionManagerGenerator.NEW

        allocateAndValidate(
            "new-licence-condition-allocation-message",
            "new-licence-condition-allocation-body",
            existingManager,
            licenceCondition,
            1
        )

        verify(telemetryService).trackEvent(
            eq("LicenceConditionAllocation"),
            eq(
                mapOf(
                    "crn" to licenceCondition.person.crn,
                    "detailUrl" to "http://localhost:${wireMockServer.port()}/allocation/licence-conditions/allocate-new-licence-condition-manager"
                )
            ),
            any()
        )
    }

    @Test
    fun `allocate historic licence condition manager`() {
        val licenceCondition = LicenceConditionGenerator.HISTORIC

        val firstLcm = licenceConditionManagerRepository.save(
            licenceConditionManagerRepository.findByIdOrNull(LicenceConditionManagerGenerator.HISTORIC.id)?.apply {
                endDate = ZonedDateTime.now().minusDays(1)
            }!!
        )
        val secondLcm =
            licenceConditionManagerRepository.save(
                LicenceConditionManagerGenerator.generate(
                    licenceConditionId = licenceCondition.id,
                    startDateTime = firstLcm.endDate!!
                )
            )

        allocateAndValidate(
            "historic-licence-condition-allocation-message",
            "historic-licence-condition-allocation-body",
            firstLcm,
            licenceCondition,
            2
        )

        val insertedLcm = licenceConditionManagerRepository.findActiveManagerAtDate(
            licenceCondition.id,
            ZonedDateTime.now().minusDays(2)
        )
        assert(secondLcm.startDate.closeTo(insertedLcm?.endDate))

        verify(telemetryService).trackEvent(
            eq("LicenceConditionAllocation"),
            eq(
                mapOf(
                    "crn" to licenceCondition.person.crn,
                    "detailUrl" to "http://localhost:${wireMockServer.port()}/allocation/licence-conditions/allocate-historic-licence-condition-manager"
                )
            ),
            any()
        )
    }

    private fun allocateAndValidate(
        messageName: String,
        jsonFile: String,
        existingLcm: LicenceConditionManager,
        licenceCondition: LicenceCondition,
        originalLcmCount: Int
    ) {
        val allocationEvent = prepMessage(messageName, wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(allocationEvent)

        val allocationDetail = ResourceLoader.file<AllocationDetail.LicenceConditionAllocation>(jsonFile)

        val updatedLcmCount = licenceConditionManagerRepository.findAll().count { it.licenceConditionId == licenceCondition.id }
        assertThat(updatedLcmCount, equalTo(originalLcmCount + 1))

        val oldLcm = licenceConditionManagerRepository.findById(existingLcm.id).orElseThrow()
        assert(allocationDetail.createdDate.closeTo(oldLcm.endDate))
    }
}

