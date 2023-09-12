package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@SpringBootTest
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired lateinit var channelManager: HmppsChannelManager

    @MockBean lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Test
    fun `process opd assessment`() {
        val message = prepMessage("opd-assessment", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(message)

        verify(telemetryService).notificationReceived(message)
    }
}
