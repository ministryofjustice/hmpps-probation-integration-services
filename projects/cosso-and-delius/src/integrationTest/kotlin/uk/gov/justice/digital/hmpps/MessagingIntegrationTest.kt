package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.DEFAULT_COSSO_CREATED
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.DEFAULT_COSSO_DELETED
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@SpringBootTest
internal class MessagingIntegrationTest : BaseIntegrationTest() {
    @Autowired
    lateinit var documentRepository: DocumentRepository

    @Test
    fun `breach notice not found`() {
        // Given a message with a detail url that returns 404
        val notification = prepEvent("cosso-notice-created", wireMockServer.port()).run {
            copy(message = message.copy(detailUrl = "http://localhost:${wireMockServer.port()}/cosso/pdf/404"))
        }

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(notification)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(notification)

        // But no changes are made
        verify(telemetryService, never()).trackEvent(eq("DocumentUploaded"), any(), any())
        wireMockServer.verify(0, anyRequestedFor(urlPathMatching("/alfresco/.*")))
    }

    @Test
    fun `document not found`() {
        // Given a message with a non-existent breach notice id
        val notification = prepEvent("cosso-notice-created", wireMockServer.port()).run {
            copy(message = message.copy(additionalInformation = mapOf("COSSOBreachNoticeId" to "99999999-9999-9999-9999-999999999999")))
        }

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(notification)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(notification)

        // But no changes are made
        verify(telemetryService, never()).trackEvent(eq("DocumentUploaded"), any(), any())
        wireMockServer.verify(0, anyRequestedFor(urlPathMatching("/alfresco/.*")))
    }

    @Test
    fun `invalid pdf`() {
        // Given a message with a detail url that returns a bad response
        val notification = prepEvent("cosso-notice-created", wireMockServer.port()).run {
            copy(message = message.copy(detailUrl = "http://localhost:${wireMockServer.port()}/cosso/pdf/invalid"))
        }

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(notification)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(notification)

        // But no changes are made
        verify(telemetryService, never()).trackEvent(eq("DocumentUploaded"), any(), any())
        wireMockServer.verify(0, anyRequestedFor(urlPathMatching("/alfresco/.*")))
    }

    @Test
    fun `breach notice is created`() {
        // Given a message
        val notification = prepEvent("cosso-notice-created", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(notification)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(notification)
        verify(telemetryService).trackEvent(
            "DocumentUploaded",
            mapOf(
                "crn" to "X123456",
                "cossoBreachNoticeId" to "00000000-0000-0000-0000-000000000001",
                "username" to "CossoAndDelius"
            ),
            mapOf()
        )

        // And the document is updated in the database
        val document = documentRepository.findById(DEFAULT_COSSO_CREATED.id).get()
        assertThat(document.name).isEqualTo("test.pdf")
        assertThat(document.status).isEqualTo("Y")
        assertThat(document.workInProgress).isEqualTo("N")
        assertThat(document.lastSaved).isCloseTo(ZonedDateTime.now(), within(1, ChronoUnit.SECONDS))
        assertThat(document.lastUpdatedUserId).isEqualTo(UserGenerator.AUDIT_USER.id)

        // And the file is uploaded to Alfresco
        wireMockServer.verify(
            postRequestedFor(urlEqualTo("/alfresco/uploadnew"))
                .withRequestBodyPart(aMultipart().withFileName("test.pdf").build())
                .withRequestBodyPart(aMultipart().withName("fileName").withBody(equalTo("test.pdf")).build())
                .withAlfrescoHeaders()
        )
    }

    @Test
    fun `breach notice is deleted`() {
        // Given a message
        val notification = prepEvent("cosso-notice-deleted", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(notification)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(notification)
        verify(telemetryService).trackEvent(
            "DocumentDeleted",
            mapOf(
                "crn" to "X123456",
                "cossoBreachNoticeId" to "00000000-0000-0000-0000-000000000003",
                "username" to "TestUser"
            ),
            mapOf()
        )

        // And the document is deleted from the database
        assertThat(documentRepository.findById(DEFAULT_COSSO_DELETED.id)).isEmpty

        // And the file is deleted from Alfresco
        wireMockServer.verify(putRequestedFor(urlEqualTo("/alfresco/release/${DEFAULT_COSSO_DELETED.alfrescoId}")).withAlfrescoHeaders())
        wireMockServer.verify(deleteRequestedFor(urlEqualTo("/alfresco/deletehard/${DEFAULT_COSSO_DELETED.alfrescoId}")).withAlfrescoHeaders())
    }

    private fun RequestPatternBuilder.withAlfrescoHeaders() = withHeader("Authorization", absent())
        .withHeader("X-DocRepository-Remote-User", equalTo("N00"))
        .withHeader("X-DocRepository-Real-Remote-User", equalTo("CossoAndDelius"))
}