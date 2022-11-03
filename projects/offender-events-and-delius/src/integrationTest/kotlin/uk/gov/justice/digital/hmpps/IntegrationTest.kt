package uk.gov.justice.digital.hmpps

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
@ActiveProfiles("integration-test")
internal class IntegrationTest {
    @Value("\${spring.jms.template.default-destination}") lateinit var queueName: String
    @Autowired lateinit var jmsTemplate: JmsTemplate
    @MockBean lateinit var telemetryService: TelemetryService
}
