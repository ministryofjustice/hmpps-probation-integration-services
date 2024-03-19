package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_WITH_NOMS
import uk.gov.justice.digital.hmpps.messaging.Handler
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = ["messaging.consumer.dry-run=false"])
internal class MergeIntegrationTest {
    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var handler: Handler

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `merge replaces noms number`() {
        handler.handle(prepEvent("prisoner-merged", wireMockServer.port()))

        verify(telemetryService).trackEvent(
            "MergeResultSuccess", mapOf(
                "reason" to "Replaced NOMS numbers for 1 records",
                "existingNomsNumber" to "A0007AA",
                "updatedNomsNumber" to "B0007BB",
                "matches" to """[{"crn":"A000007"}]""",
                "dryRun" to "false",
            )
        )
    }

    @Test
    fun `merge fails if the new noms number is already assigned`() {
        val event = prepEvent("prisoner-merged", wireMockServer.port()).apply {
            message.additionalInformation["nomsNumber"] = PERSON_WITH_NOMS.nomsNumber!!
        }

        val exception = assertThrows<IllegalArgumentException> { handler.handle(event) }

        assertThat(exception.message, equalTo("NOMS number E1234XS is already assigned to A000001"))
    }

    @Test
    fun `merge ignored if the old noms number is not in Delius`() {
        val event = prepEvent("prisoner-merged", wireMockServer.port()).apply {
            message.additionalInformation["removedNomsNumber"] = "Z9999ZZ"
        }

        handler.handle(event)

        verify(telemetryService).trackEvent(
            "MergeResultIgnored", mapOf(
                "reason" to "No records found for NOMS number Z9999ZZ",
                "dryRun" to "false",
            )
        )
    }
}
