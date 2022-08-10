package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.jms.convertSendAndWait
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ActiveProfiles("integration-test")
@SpringBootTest
class PsrCompletedIntegrationTest {

    @Value("\${spring.jms.template.default-destination}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Test
    fun `completed pre sentence report`() {

        jmsTemplate.convertSendAndWait(queueName, prepMessage("psr-message", wireMockServer.port()))

        verify(telemetryService).hmppsEventReceived(any())
    }
}
