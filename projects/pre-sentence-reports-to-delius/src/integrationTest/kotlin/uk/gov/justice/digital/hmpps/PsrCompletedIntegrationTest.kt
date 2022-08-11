package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoClient
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

    @SpyBean
    private lateinit var alfrescoClient: AlfrescoClient

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `completed pre sentence report`() {
        val reportId = "f9b09fcf-39c0-4008-8b43-e616ddfd918c"
        val document = documentRepository.findByExternalReference(reportId)

        val message = prepMessage("psr-message", wireMockServer.port())

        jmsTemplate.convertSendAndWait(queueName, message)

        verify(telemetryService).hmppsEventReceived(any())

        verify(alfrescoClient).releaseDocument(DocumentGenerator.DEFAULT.alfrescoId)
        verify(alfrescoClient).updateDocument(eq(DocumentGenerator.DEFAULT.alfrescoId), any())

        val updated = documentRepository.findByExternalReference(reportId)
        assertThat(updated?.lastSaved, greaterThan(document?.lastSaved))
    }
}
