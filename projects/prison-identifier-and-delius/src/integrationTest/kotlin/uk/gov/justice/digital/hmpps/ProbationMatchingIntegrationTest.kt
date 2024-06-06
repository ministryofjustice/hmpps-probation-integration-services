package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON
import com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.entity.AdditionalIdentifierRepository
import uk.gov.justice.digital.hmpps.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ProbationMatchingIntegrationTest {

    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @SpyBean
    lateinit var personRepository: PersonRepository

    @SpyBean
    lateinit var custodyRepository: CustodyRepository

    @SpyBean
    lateinit var additionalIdentifierRepository: AdditionalIdentifierRepository

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `inactive booking is ignored`() {
        val event = prepEvent("prisoner-status-changed-inactive-booking", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent(
            "MatchResultIgnored",
            mapOf("reason" to "No active booking", "dryRun" to "false")
        )
    }

    @Test
    @Order(1)
    fun `prisoner received updates identifiers`() {
        withMatchResponse("probation-search-single-result.json")

        val event = prepEvent("prisoner-status-changed", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent(
            "MatchResultSuccess", mapOf(
                "reason" to "Matched CRN A000001 to NOMS number A0001AA and custody ${custodyId("A000001")} to 00001A",
                "dryRun" to "false",
                "nomsNumber" to "A0001AA",
                "bookingNo" to "00001A",
                "matchedBy" to "ALL_SUPPLIED",
                "potentialMatches" to """[{"crn":"A000001"}]""",
                "existingNomsNumber" to "E1234XS",
                "matchedNomsNumber" to "A0001AA",
                "nomsNumberChanged" to "true",
                "matchedBookingNumber" to "00001A",
                "bookingNumberChanged" to "true",
                "custody" to "${custodyId("A000001")}",
                "sentenceDateInDelius" to "2022-11-11",
                "sentenceDateInNomis" to "2022-11-11",
                "totalCustodialEvents" to "1",
                "matchingCustodialEvents" to "1"
            )
        )
    }

    @Test
    @Order(2)
    fun `multiple matches are refined by sentence date`() {
        withMatchResponse("probation-search-multiple-results.json")

        val event = prepEvent("prisoner-status-changed", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent(
            "MatchResultSuccess", mapOf(
                "reason" to "Matched CRN A000001 to NOMS number A0001AA and custody ${custodyId("A000001")} to 00001A",
                "dryRun" to "false",
                "nomsNumber" to "A0001AA",
                "bookingNo" to "00001A",
                "matchedBy" to "ALL_SUPPLIED",
                "potentialMatches" to """[{"crn":"A000002"},{"crn":"A000001"}]""",
                "existingNomsNumber" to "A0001AA",
                "matchedNomsNumber" to "A0001AA",
                "nomsNumberChanged" to "false",
                "existingBookingNumber" to "00001A",
                "matchedBookingNumber" to "00001A",
                "bookingNumberChanged" to "false",
                "custody" to "${custodyId("A000001")}",
                "sentenceDateInDelius" to "2022-11-11",
                "sentenceDateInNomis" to "2022-11-11",
                "totalCustodialEvents" to "1",
                "matchingCustodialEvents" to "1"
            )
        )
    }

    @Test
    fun `no matches from probation search`() {
        withMatchResponse("probation-search-no-results.json")

        val event = prepEvent("prisoner-status-changed", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent(
            "MatchResultNoMatch", mapOf(
                "reason" to "No single match found in probation system",
                "dryRun" to "false",
                "nomsNumber" to "A0001AA",
                "bookingNo" to "00001A",
                "matchedBy" to "NONE",
                "potentialMatches" to "[]",
                "sentenceDateInNomis" to "2022-11-11"
            )
        )
    }

    @Test
    @DirtiesContext
    fun `no matches on sentence date`() {
        withMatchResponse("probation-search-single-result.json")
        withJsonResponse(
            "/prison-api/api/offender-sentences/booking/10000001/sentenceTerms",
            "prison-api-A0001AA-unmatched-sentence-date.json"
        )

        val event = prepEvent("prisoner-status-changed", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent(
            "MatchResultNoMatch", mapOf(
                "reason" to "No single match found in probation system",
                "dryRun" to "false",
                "nomsNumber" to "A0001AA",
                "bookingNo" to "00001A",
                "matchedBy" to "ALL_SUPPLIED",
                "potentialMatches" to """[{"crn":"A000001"}]""",
                "sentenceDateInNomis" to "2021-01-01"
            )
        )
    }

    private fun withJsonResponse(url: String, filename: String) {
        val response = aResponse().withStatus(200).withBodyFile(filename).withHeader(CONTENT_TYPE, APPLICATION_JSON)
        wireMockServer.addStubMapping(get(url).willReturn(response).build())
    }

    private fun withMatchResponse(filename: String) {
        val response = aResponse().withStatus(200).withBodyFile(filename).withHeader(CONTENT_TYPE, APPLICATION_JSON)
        wireMockServer.addStubMapping(post("/probation-search/match").willReturn(response).build())
    }

    private fun custodyId(crn: String): Long = personRepository.findSentencedByCrn(crn).first().custody!!.id
}
