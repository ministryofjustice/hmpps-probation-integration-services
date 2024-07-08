package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
class PsrCompletedIntegrationTest {

    @Value("\${messaging.consumer.queue}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var channelManager: HmppsChannelManager

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired
    private lateinit var documentRepository: DocumentRepository

    @SpyBean
    private lateinit var alfrescoUploadClient: AlfrescoUploadClient

    @Test
    fun `completed pre sentence report`() {
        val reportId = "f9b09fcf-39c0-4008-8b43-e616ddfd918c"
        val document = documentRepository.findByExternalReference(reportId)

        val message = prepMessage("psr-message", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(message)

        verify(telemetryService).notificationReceived(message)

        verify(alfrescoUploadClient).releaseDocument(DocumentGenerator.DEFAULT.alfrescoId)
        verify(alfrescoUploadClient).updateDocument(eq(DocumentGenerator.DEFAULT.alfrescoId), any())

        val updated = documentRepository.findByExternalReference(reportId)
        assertThat(updated?.lastSaved, greaterThanOrEqualTo(document?.lastSaved))
    }
}
