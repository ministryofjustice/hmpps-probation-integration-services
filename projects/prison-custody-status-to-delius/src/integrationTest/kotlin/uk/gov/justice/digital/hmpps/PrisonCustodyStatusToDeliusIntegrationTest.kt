package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.jms.convertSendAndWait
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
@ActiveProfiles("integration-test")
internal class PrisonCustodyStatusToDeliusIntegrationTest {

    @Value("\${spring.jms.template.default-destination}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `release a prisoner`() {
        val message = MessageGenerator.PRISONER_RELEASED

        jmsTemplate.convertSendAndWait(queueName, message)

        verify(telemetryService).trackEvent(
            "PrisonerReleased",
            mapOf(
                "nomsNumber" to "A0001AA",
                "institution" to "WSI",
                "reason" to "RELEASED",
                "details" to "Movement reason code NCS"
            )
        )
    }

    @Test
    fun `recall a prisoner`() {
        val message = MessageGenerator.PRISONER_RECEIVED

        jmsTemplate.convertSendAndWait(queueName, message)

        verify(telemetryService).trackEvent(
            "PrisonerRecalled",
            mapOf(
                "nomsNumber" to "A0001AA",
                "institution" to "WSI",
                "reason" to "ADMISSION",
                "details" to "ACTIVE IN:ADM-L"
            )
        )
    }
}
