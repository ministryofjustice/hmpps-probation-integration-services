package uk.gov.justice.digital.hmpps

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@SpringBootTest
internal class BaseIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    internal lateinit var queueName: String

    @Autowired
    internal lateinit var channelManager: HmppsChannelManager

    @Autowired
    internal lateinit var mockMvc: MockMvc

    @MockitoBean
    internal lateinit var telemetryService: TelemetryService
}