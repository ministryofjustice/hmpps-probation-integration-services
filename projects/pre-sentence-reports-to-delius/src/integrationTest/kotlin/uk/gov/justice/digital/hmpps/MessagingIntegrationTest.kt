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
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.FINAL_DOCUMENT
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.entity.CourtReportRepository
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

internal class MessagingIntegrationTest @Autowired constructor(
    private val documentRepository: DocumentRepository,
    private val courtReportRepository: CourtReportRepository
) : BaseIntegrationTest() {

    @Test
    fun `psr not found`() {
        // Given a message with a detail url that returns 404
        val notification = prepEvent("pre-sentence-report-created", wireMockServer.port()).run {
            copy(message = message.copy(detailUrl = "http://localhost:${wireMockServer.port()}/pre-sentence-report/pdf/404"))
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
        // Given a message with a non-existent psr id
        val notification = prepEvent("pre-sentence-report-created", wireMockServer.port()).run {
            copy(message = message.copy(additionalInformation = mapOf("psrId" to "99999999-9999-9999-9999-999999999999")))
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
        val notification = prepEvent("pre-sentence-report-created", wireMockServer.port()).run {
            copy(message = message.copy(detailUrl = "http://localhost:${wireMockServer.port()}/pre-sentence-report/pdf/invalid"))
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
    fun `psr is created`() {
        // Given a message
        val notification = prepEvent("pre-sentence-report-created", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(notification)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(notification)
        verify(telemetryService).trackEvent(
            "DocumentUploaded",
            mapOf(
                "crn" to "X012771",
                "psrId" to "00000000-0000-0000-0000-000000000001",
                "username" to "TestUser"
            ),
            mapOf()
        )

        // And the document is updated in the database
        val document = documentRepository.findById(FINAL_DOCUMENT.id).get()
        assertThat(document.name).isEqualTo("Final Person's court report.pdf")
        assertThat(document.status).isEqualTo("Y")
        assertThat(document.workInProgress).isEqualTo("N")
        assertThat(document.lastSaved).isCloseTo(ZonedDateTime.now(), within(1, ChronoUnit.SECONDS))
        assertThat(document.lastUpdatedUserId).isEqualTo(UserGenerator.TEST_USER.id)
        // check the court report is marked as completed
        val courtReport = courtReportRepository.findById(FINAL_DOCUMENT.courtReport.id).get()
        assertThat(courtReport.completedDate).isCloseTo(LocalDate.now(), within(1, ChronoUnit.DAYS))

        // And the file is uploaded to Alfresco
        wireMockServer.verify(
            postRequestedFor(urlEqualTo("/alfresco/uploadnew"))
                .withRequestBodyPart(aMultipart().withFileName("Final Person's court report.pdf").build())
                .withRequestBodyPart(
                    aMultipart().withName("fileName").withBody(equalTo("Final Person's court report.pdf")).build()
                )
                .withAlfrescoHeaders()
        )
    }

    private fun RequestPatternBuilder.withAlfrescoHeaders() = withHeader("Authorization", absent())
        .withHeader("X-DocRepository-Remote-User", equalTo("N00"))
        .withHeader("X-DocRepository-Real-Remote-User", equalTo("PreSentenceService"))
}