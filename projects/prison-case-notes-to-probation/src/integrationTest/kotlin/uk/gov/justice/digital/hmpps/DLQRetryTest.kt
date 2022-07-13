package uk.gov.justice.digital.hmpps

import io.specto.hoverfly.junit.core.Hoverfly
import io.specto.hoverfly.junit.core.HoverflyMode
import io.specto.hoverfly.junit5.HoverflyExtension
import io.specto.hoverfly.junit5.api.HoverflyConfig
import io.specto.hoverfly.junit5.api.HoverflyCore
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.SimulationBuilder
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteMessageGenerator
import java.time.Duration
import java.time.LocalDateTime.now

@ActiveProfiles("integration-test")
@SpringBootTest(properties = ["spring.jms.template.delivery-delay=1s"])
@HoverflyCore(
    mode = HoverflyMode.SIMULATE,
    config = HoverflyConfig(adminPort = 8888, proxyPort = 8500, webServer = true)
)
@ExtendWith(HoverflyExtension::class)
class DLQRetryTest {

    @Value("\${integrations.prison-offender-events.queue}")
    private lateinit var queueName: String

    @Value("\${integrations.prison-offender-events.dlq}")
    private lateinit var dlqName: String

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var simBuilder: SimulationBuilder

    @Autowired
    private lateinit var embeddedActiveMQ: EmbeddedActiveMQ

    @BeforeEach
    fun setUp(hoverfly: Hoverfly) {
        val sources = simBuilder.simulationsFromFile()
        if (sources.isNotEmpty()) {
            hoverfly.simulate(
                sources.first(),
                *sources.drop(1).toTypedArray()
            )
        }
    }

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
