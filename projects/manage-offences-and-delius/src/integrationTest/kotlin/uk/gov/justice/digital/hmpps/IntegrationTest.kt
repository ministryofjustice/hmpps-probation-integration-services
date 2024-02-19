package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.message.Notification
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

    @Test
    fun `update offence code`() {
        val notification = Notification(ResourceLoader.event("offence-changed"))

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent("OffenceCodeUpdated", mapOf("offenceCode" to "AB06001"), mapOf())
    }
}
