package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_WITH_NOMS
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = ["messaging.consumer.dry-run=false"])
internal class MergeIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `merge replaces noms number`() {
        val event = prepEvent("prisoner-merged", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent(
            "MergeResultSuccess", mapOf(
                "reason" to "Replaced NOMS numbers for 1 record",
                "existingNomsNumber" to "A0007AA",
                "updatedNomsNumber" to "B0007BB",
                "matches" to """[{"crn":"A000007"}]""",
                "dryRun" to "false",
            )
        )
    }

    @Test
    fun `logs event if the new noms number is already assigned`() {
        val event = prepEvent("prisoner-merged", wireMockServer.port()).apply {
            message.additionalInformation["nomsNumber"] = PERSON_WITH_NOMS.nomsNumber!!
        }

        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent(
            "MergeResultIgnored", mapOf(
                "reason" to "NOMS number E1234XS is already assigned to A000001",
                "dryRun" to "false",
            )
        )
    }

    @Test
    fun `merge ignored if the old noms number is not in Delius`() {
        val event = prepEvent("prisoner-merged", wireMockServer.port()).apply {
            message.additionalInformation["removedNomsNumber"] = "Z9999ZZ"
        }

        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent(
            "MergeResultIgnored", mapOf(
                "reason" to "No records found for NOMS number Z9999ZZ",
                "dryRun" to "false",
            )
        )
    }
}
