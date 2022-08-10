package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
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

    @Autowired
    private lateinit var documentRepository: DocumentRepository

    @Test
    fun `completed pre sentence report`() {

        val message = prepMessage("psr-message", wireMockServer.port())

        jmsTemplate.convertSendAndWait(queueName, message)

        verify(telemetryService).hmppsEventReceived(any())

        val reportId = message.additionalInformation["reportId"] as String
        val document = documentRepository.findByExternalReference(reportId)

        val filename = "${PersonGenerator.DEFAULT.crn}_pre-sentence-report_$reportId.pdf"
        assertThat(document?.name, equalTo(filename))
    }
}
