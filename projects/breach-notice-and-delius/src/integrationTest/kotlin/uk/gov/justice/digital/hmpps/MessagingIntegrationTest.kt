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
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.DEFAULT_BREACH_NOTICE
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.DELETED_BREACH_NOTICE
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.DocumentRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

internal class MessagingIntegrationTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var contactRepository: ContactRepository

    @Autowired
    private lateinit var documentRepository: DocumentRepository

    @Test
    fun `breach notice not found`() {
        // Given a message with a detail url that returns 404
        val notification = prepEvent("breach-notice-created", wireMockServer.port()).run {
            copy(message = message.copy(detailUrl = "http://localhost:${wireMockServer.port()}/breach-notice/pdf/404"))
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
        val notification = prepEvent("breach-notice-created", wireMockServer.port()).run {
            copy(message = message.copy(additionalInformation = mapOf("breachNoticeId" to UUID.fromString("99999999-9999-9999-9999-999999999999"))))
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
        val notification = prepEvent("breach-notice-created", wireMockServer.port()).run {
            copy(message = message.copy(detailUrl = "http://localhost:${wireMockServer.port()}/breach-notice/pdf/invalid"))
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
        val notification = prepEvent("breach-notice-created", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(notification)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(notification)
        verify(telemetryService).trackEvent(
            "DocumentUploaded",
            mapOf(
                "crn" to "A000001",
                "breachNoticeId" to "00000000-0000-0000-0000-000000000001",
                "username" to "TestUser"
            ),
            mapOf()
        )

        // And the document is updated in the database
        val document = documentRepository.findById(DEFAULT_BREACH_NOTICE.id).get()
        assertThat(document.name).isEqualTo("name.pdf")
        assertThat(document.status).isEqualTo("Y")
        assertThat(document.workInProgress).isEqualTo("N")
        assertThat(document.lastSaved).isCloseTo(ZonedDateTime.now(), within(1, ChronoUnit.SECONDS))
        assertThat(document.lastUpdatedUserId).isEqualTo(UserGenerator.TEST_USER.id)

        // And the file is uploaded to Alfresco
        wireMockServer.verify(postRequestedFor(urlEqualTo("/alfresco/uploadandrelease/${document.alfrescoId}"))
            .withRequestBodyPart(aMultipart().withFileName("name.pdf").build())
            .withRequestBodyPart(aMultipart().withName("fileName").withBody(equalTo("name.pdf")).build())
            .withAlfrescoHeaders())
        wireMockServer.verify(putRequestedFor(urlEqualTo("/alfresco/lock/${document.alfrescoId}")).withAlfrescoHeaders())
    }

    @Test
    fun `breach notice is deleted`() {
        // Given a message
        val notification = prepEvent("breach-notice-deleted", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(notification)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(notification)
        verify(telemetryService).trackEvent(
            "DocumentDeleted",
            mapOf(
                "crn" to "A000001",
                "breachNoticeId" to "00000000-0000-0000-0000-000000000003",
                "username" to "TestUser"
            ),
            mapOf()
        )

        // And the document is deleted from the database
        assertThat(documentRepository.findById(DELETED_BREACH_NOTICE.id)).isEmpty
        assertThat(contactRepository.findById(DELETED_BREACH_NOTICE.primaryKeyId).get().documentLinked).isEqualTo(false)

        // And the file is deleted from Alfresco
        wireMockServer.verify(putRequestedFor(urlEqualTo("/alfresco/release/${DELETED_BREACH_NOTICE.alfrescoId}")).withAlfrescoHeaders())
        wireMockServer.verify(deleteRequestedFor(urlEqualTo("/alfresco/deletehard/${DELETED_BREACH_NOTICE.alfrescoId}")).withAlfrescoHeaders())
    }

    private fun RequestPatternBuilder.withAlfrescoHeaders() = withHeader("Authorization", absent())
        .withHeader("X-DocRepository-Remote-User", equalTo("N00"))
        .withHeader("X-DocRepository-Real-Remote-User", equalTo("BreachNoticeAndDelius"))
}