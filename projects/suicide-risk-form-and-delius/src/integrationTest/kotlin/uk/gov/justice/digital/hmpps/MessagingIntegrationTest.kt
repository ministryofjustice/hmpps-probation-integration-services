package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.DocumentRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@SpringBootTest
internal class MessagingIntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}")
    internal val queueName: String,
    internal val channelManager: HmppsChannelManager,
    internal val wireMockServer: WireMockServer,
    private val documentRepository: DocumentRepository,
    private val contactRepository: ContactRepository
) {
    @MockitoBean
    internal lateinit var telemetryService: TelemetryService

    @Test
    fun `suicide risk form is created`() {
        val notification = prepEvent("suicide-risk-form-created", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(telemetryService).trackEvent(
            "DocumentUploaded",
            mapOf(
                "crn" to "A000001",
                "suicideRiskFormId" to "00000000-0000-0000-0000-000000000001",
                "username" to "officer"
            ),
            mapOf()
        )

        val document = documentRepository.findById(DocumentGenerator.DEFAULT_SUICIDE_RISK_FORM.id).get()
        assertThat(document.name).isEqualTo("srf.pdf")
        assertThat(document.status).isEqualTo("Y")
        assertThat(document.workInProgress).isEqualTo("N")
        assertThat(document.lastSaved).isCloseTo(ZonedDateTime.now(), within(1, ChronoUnit.SECONDS))
        assertThat(document.lastUpdatedUserId).isEqualTo(UserGenerator.DEFAULT.id)

        wireMockServer.verify(
            postRequestedFor(urlEqualTo("/alfresco/uploadnew"))
                .withRequestBodyPart(aMultipart().withFileName("srf.pdf").build())
                .withRequestBodyPart(aMultipart().withName("fileName").withBody(equalTo("srf.pdf")).build())
                .withAlfrescoHeaders()
        )
    }

    @Test
    fun `suicide risk form is deleted`() {
        val notification = prepEvent("suicide-risk-form-deleted", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(telemetryService).trackEvent(
            "DocumentDeleted",
            mapOf(
                "crn" to "A000001",
                "suicideRiskFormId" to "00000000-0000-0000-0000-000000000002",
                "username" to "officer"
            ),
            mapOf()
        )

        assertThat(documentRepository.findById(DocumentGenerator.DELETED_SUICIDE_RISK_FORM.id)).isEmpty
        assertThat(
            contactRepository.findById(DocumentGenerator.DELETED_SUICIDE_RISK_FORM.primaryKeyId).get().documentLinked
        ).isEqualTo(false)

        wireMockServer.verify(putRequestedFor(urlEqualTo("/alfresco/release/${DocumentGenerator.DELETED_SUICIDE_RISK_FORM.alfrescoId}")).withAlfrescoHeaders())
        wireMockServer.verify(deleteRequestedFor(urlEqualTo("/alfresco/deletehard/${DocumentGenerator.DELETED_SUICIDE_RISK_FORM.alfrescoId}")).withAlfrescoHeaders())
    }

    @Test
    @Order(1)
    fun `suicide risk form not found`() {
        val notification = prepEvent("suicide-risk-form-created", wireMockServer.port()).run {
            copy(message = message.copy(detailUrl = "http://localhost:${wireMockServer.port()}/suicide-risk-form/pdf/404"))
        }

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(telemetryService, never()).trackEvent(eq("DocumentUploaded"), any(), any())
        wireMockServer.verify(0, anyRequestedFor(urlPathMatching("/alfresco/.*")))
    }

    @Test
    fun `document not found`() {
        val notification = prepEvent("suicide-risk-form-created", wireMockServer.port()).run {
            copy(message = message.copy(additionalInformation = mapOf("suicideRiskFormId" to UUID.fromString("99999999-9999-9999-9999-999999999999"))))
        }

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).notificationReceived(notification)

        verify(telemetryService, never()).trackEvent(eq("DocumentUploaded"), any(), any())
        wireMockServer.verify(0, anyRequestedFor(urlPathMatching("/alfresco/.*")))
    }

    @Test
    fun `invalid pdf`() {
        val notification = prepEvent("suicide-risk-form-created", wireMockServer.port()).run {
            copy(message = message.copy(detailUrl = "http://localhost:${wireMockServer.port()}/suicide-risk-form/pdf/invalid"))
        }

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).notificationReceived(notification)

        verify(telemetryService, never()).trackEvent(eq("DocumentUploaded"), any(), any())
        wireMockServer.verify(0, anyRequestedFor(urlPathMatching("/alfresco/.*")))
    }

    private fun RequestPatternBuilder.withAlfrescoHeaders() = withHeader("Authorization", absent())
        .withHeader("X-DocRepository-Remote-User", equalTo("N00"))
        .withHeader("X-DocRepository-Real-Remote-User", equalTo("SuicideRiskFormAndDelius"))
}