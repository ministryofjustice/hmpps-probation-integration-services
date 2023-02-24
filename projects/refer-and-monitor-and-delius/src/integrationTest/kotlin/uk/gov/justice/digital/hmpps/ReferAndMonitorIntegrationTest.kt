package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.messaging.DomainEventType
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader.notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
@ActiveProfiles("integration-test")
internal class ReferAndMonitorIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `session appointment feedback submitted`() {
        val notification = prepNotification(
            notification("session-appointment-feedback-submitted"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent(
            "UnhandledEventReceived",
            mapOf(
                "eventType" to DomainEventType.SessionAppointmentSubmitted.name
            )
        )
    }

    @Test
    fun `referral end submitted`() {
        val notification = prepNotification(
            notification("referral-prematurely-ended"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent(
            "ReferralEnded",
            mapOf(
                "crn" to "T140223",
                "referralUrn" to "urn:hmpps:interventions-referral:68df9f6c-3fcb-4ec6-8fcf-96551cd9b080",
                "endDate" to "2023-02-23T15:29:54.197Z[Europe/London]",
                "endType" to "PREMATURELY_ENDED"
            )
        )
    }
}
