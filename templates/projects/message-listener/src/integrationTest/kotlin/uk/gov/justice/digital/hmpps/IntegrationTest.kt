package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.mockito.Mockito.atLeastOnce
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.jms.convertSendAndWait
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.util.concurrent.TimeoutException

@SpringBootTest
@ActiveProfiles("integration-test")
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}") lateinit var queueName: String
    @Autowired lateinit var jmsTemplate: JmsTemplate
    @MockBean lateinit var telemetryService: TelemetryService

    @Test
    fun `message is logged to telemetry`() {
        // Given a message
        val notification = Notification(message = MessageGenerator.EXAMPLE)

        // When it is received
        try {
            jmsTemplate.convertSendAndWait(queueName, notification)
        } catch (_: TimeoutException) {
            // Note: Remove this try/catch when the MessageListener logic has been implemented
        }

        // Then it is logged to telemetry
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
    }
}
