package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.mockito.Mockito.atLeastOnce
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
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

        verify(telemetryService, atLeastOnce()).trackEvent(
            "SessionAppointmentSubmitted",
            mapOf(
                "appointmentId" to "1824573421",
                "crn" to "T140223",
                "referralId" to "f56c5f7c-632f-4cad-a1b3-693541cb5f22"
            )
        )
    }
}
