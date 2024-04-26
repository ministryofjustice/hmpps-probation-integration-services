package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.atMost
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.entity.OffenceRepository
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.FF_CREATE_OFFENCE
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @MockBean
    lateinit var telemetryService: TelemetryService

    @MockBean
    lateinit var featureFlags: FeatureFlags

    @Autowired
    lateinit var offenceRepository: OffenceRepository

    @Test
    fun `update offence code`() {
        whenever(featureFlags.enabled(FF_CREATE_OFFENCE)).thenReturn(true)
        val notification = Notification(ResourceLoader.event("offence-changed"))

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify()

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify()
    }

    private fun verify() {
        verify(telemetryService, atMost(2)).trackEvent("OffenceCodeUpdated", mapOf("offenceCode" to "AB06001"), mapOf())

        val referenceOffence = offenceRepository.findOffenceByCode("09155")
        assertNotNull(referenceOffence)

        assertThat(referenceOffence?.description).isEqualTo("Obstructing a person home office description")
        assertThat(referenceOffence?.mainCategoryCode).isEqualTo("091")
        assertThat(referenceOffence?.subCategoryCode).isEqualTo("55")
        assertThat(referenceOffence?.schedule15ViolentOffence).isEqualTo(true)
        assertThat(referenceOffence?.schedule15SexualOffence).isEqualTo(false)
    }
}
