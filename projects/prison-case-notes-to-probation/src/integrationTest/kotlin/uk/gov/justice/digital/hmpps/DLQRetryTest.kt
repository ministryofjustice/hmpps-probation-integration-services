package uk.gov.justice.digital.hmpps

import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteMessageGenerator
import java.time.Duration
import java.time.LocalDateTime.now

@ActiveProfiles("integration-test")
@SpringBootTest(
    properties = [
        "spring.jms.template.delivery-delay=1s",
        "wiremock.port=7978" // TODO replace this once we've moved to using a dynamic wiremock port.
    ]
)
class DLQRetryTest {

    @Value("\${integrations.prison-offender-events.queue}")
    private lateinit var queueName: String

    @Value("\${integrations.prison-offender-events.dlq}")
    private lateinit var dlqName: String

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var embeddedActiveMQ: EmbeddedActiveMQ

    @Test
    fun `dlq messages are retried after a delay`() {
        // given a queue and a dead-letter queue
        val queue = embeddedActiveMQ.activeMQServer.locateQueue(queueName)
        val dlq = embeddedActiveMQ.activeMQServer.locateQueue(dlqName)

        // when a re-processable message is sent to the DLQ
        jmsTemplate.convertSendAndWait(dlq, CaseNoteMessageGenerator.NEW_TO_DELIUS)

        // then the message will be resent to the real queue immediately
        assertThat(queue.messagesAdded, equalTo(1))

        // but the message will not be re-processed until after the configured delay
        // (the delay is set to 1 second at the top of this test class, however it will be a higher value in reality)
        val startTime = now()
        assertThat(queue.messagesAcknowledged, equalTo(0))
        queue.waitForMessagesToBeAcknowledged()
        assertThat(queue.messagesAcknowledged, equalTo(1))
        assertThat(Duration.between(startTime, now()), greaterThanOrEqualTo(Duration.ofSeconds(1)))
    }
}
