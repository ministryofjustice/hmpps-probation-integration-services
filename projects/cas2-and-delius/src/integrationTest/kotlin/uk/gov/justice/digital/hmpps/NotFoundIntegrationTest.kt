package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest(properties = ["event.exception.throw-not-found: false"])
@ExtendWith(OutputCaptureExtension::class)
internal class NotFoundIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `application submitted not found enabled`(output: CapturedOutput) {
        // Given a message
        val event = prepEvent("application-submitted-not-found", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)
        //Assert that expected exception exists in output
        assertThat(output.all, not(containsString("No DomainEvent with an ID of 3333 could be found")))
        //Assert that only 1 trackEvent for Notification Received has occurred
        verify(telemetryService, Mockito.times(1)).trackEvent(any(), any(), any())
    }

    @Test
    fun `application status not found enabled`(output: CapturedOutput) {
        // Given a message
        val event = prepEvent("application-status-updated-not-found", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)
        //Assert that expected exception exists in output
        assertThat(output.all, not(containsString("No DomainEvent with an ID of 4444 could be found")))
        //Assert that only 1 trackEvent for Notification Received has occurred
        verify(telemetryService, Mockito.times(1)).trackEvent(any(), any(), any())
    }

    @Test
    fun `application submitted bad request still thrown`(output: CapturedOutput) {
        // Given a message
        val event = prepEvent("application-submitted-bad-request", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)
        //Assert that expected exception exists in output
        assertThat(output.all, containsString("Bad Request"))
        //Assert that only 1 trackEvent for Notification Received has occurred
        verify(telemetryService, Mockito.times(1)).trackEvent(any(), any(), any())
    }
}
