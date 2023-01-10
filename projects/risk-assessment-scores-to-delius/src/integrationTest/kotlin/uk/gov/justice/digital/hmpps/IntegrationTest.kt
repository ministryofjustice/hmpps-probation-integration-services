package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.nio.file.Files

@SpringBootTest
@ActiveProfiles("integration-test")
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    private lateinit var channelManager: HmppsChannelManager

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Autowired
    private lateinit var handler: NotificationHandler<HmppsDomainEvent>

    @Test
    fun `successfully update RSR scores`() {
        val notification = Notification(
            message = MessageGenerator.RSR_SCORES_DETERMINED,
            attributes = MessageAttributes("risk-assessment.scores.determined")
        )
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService).trackEvent("RsrScoresUpdated", notification.message.telemetryProperties())
    }

    @Test
    fun `JsonMappingException handled gracefully`() {
        val message = Files.readString(ResourceUtils.getFile("classpath:messages/no-event-number.json").toPath())
        assertDoesNotThrow { handler.handle(message) }
        verify(telemetryService).trackEvent(eq("JsonMappingException"), any(), any())
    }
}
