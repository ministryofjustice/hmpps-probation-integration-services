package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.jms.convertSendAndWait
import uk.gov.justice.digital.hmpps.listener.telemetryProperties
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
@ActiveProfiles("integration-test")
internal class IntegrationTest {
    @Value("\${spring.jms.template.default-destination}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `successfully update RSR scores`() {
        val message = MessageGenerator.RSR_SCORES_DETERMINED
        jmsTemplate.convertSendAndWait(queueName, message)
        telemetryService.trackEvent("RsrScoresUpdated", message.telemetryProperties())
    }
}
