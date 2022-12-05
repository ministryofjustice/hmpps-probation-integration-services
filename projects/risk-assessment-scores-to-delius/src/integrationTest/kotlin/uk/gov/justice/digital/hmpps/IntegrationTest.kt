package uk.gov.justice.digital.hmpps

import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.jms.convertSendAndWait
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
@ActiveProfiles("integration-test")
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}") lateinit var queueName: String
    @Autowired lateinit var embeddedActiveMQ: EmbeddedActiveMQ
    @Autowired lateinit var jmsTemplate: JmsTemplate
    @MockBean lateinit var telemetryService: TelemetryService

    @Test
    fun `successfully update RSR scores`() {
        val notification = Notification(
            message = MessageGenerator.RSR_SCORES_DETERMINED,
            attributes = MessageAttributes("risk-assessment.scores.determined")
        )
        jmsTemplate.convertSendAndWait(embeddedActiveMQ, queueName, notification)
        verify(telemetryService).trackEvent("RsrScoresUpdated", notification.message.telemetryProperties())
    }
}
