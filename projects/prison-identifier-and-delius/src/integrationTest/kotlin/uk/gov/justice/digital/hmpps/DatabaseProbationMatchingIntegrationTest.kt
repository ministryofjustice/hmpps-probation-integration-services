package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON
import com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.Mockito.anyMap
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.entity.AdditionalIdentifierRepository
import uk.gov.justice.digital.hmpps.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class DatabaseProbationMatchingIntegrationTest {

    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @MockitoSpyBean
    lateinit var personRepository: PersonRepository

    @MockitoSpyBean
    lateinit var custodyRepository: CustodyRepository

    @MockitoSpyBean
    lateinit var additionalIdentifierRepository: AdditionalIdentifierRepository

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @MockitoSpyBean
    lateinit var featureFlags: FeatureFlags

    @BeforeEach
    fun setUp() {
        whenever(featureFlags.enabled("prison-identifiers-use-db-search")).thenReturn(true)
        whenever(featureFlags.enabled("prison-identifiers-compare-with-db-search")).thenReturn(false)
    }

    @Test
    @Order(1)
    fun `prisoner received updates identifiers db full match`() {
        withMatchResponse("probation-search-single-result-db-compare.json")
        withJsonResponse(
            "/prison-api/api/prisoners/A0010DB",
            "prison-api-A0010DB-prisoner.json"
        )

        val event = prepEvent("prisoner-status-changed-db", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent(
            "MatchResultSuccess", mapOf(
                "reason" to "Matched CRN A000010 to NOMS number A0010DB and custody ${custodyId("A000010")} to 00001A",
                "dryRun" to "false",
                "nomsNumber" to "A0010DB",
                "bookingNo" to "00001A",
                "matchedBy" to "ALL_SUPPLIED",
                "potentialMatches" to """[{"crn":"A000010"}]""",
                "existingNomsNumber" to "A0010DB",
                "matchedNomsNumber" to "A0010DB",
                "nomsNumberChanged" to "false",
                "matchedBookingNumber" to "00001A",
                "bookingNumberChanged" to "true",
                "custody" to "${custodyId("A000010")}",
                "sentenceDateInDelius" to "2022-11-11",
                "sentenceDateInNomis" to "2022-11-11",
                "totalCustodialEvents" to "1",
                "matchingCustodialEvents" to "1"
            )
        )
    }

    @Test
    @Order(2)
    fun `prisoner received updates identifiers db alias match`() {
        withMatchResponse("probation-search-single-result-db-compare.json")
        withJsonResponse(
            "/prison-api/api/prisoners/A0010DB",
            "prison-api-A0010DB-prisoner-alias-match.json"
        )

        val event = prepEvent("prisoner-status-changed-db", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent(
            "MatchResultSuccess", mapOf(
                "reason" to "Matched CRN A000010 to NOMS number A0010DB and custody ${custodyId("A000010")} to 00001A",
                "dryRun" to "false",
                "nomsNumber" to "A0010DB",
                "bookingNo" to "00001A",
                "matchedBy" to "ALL_SUPPLIED_ALIAS",
                "potentialMatches" to """[{"crn":"A000010"}]""",
                "existingNomsNumber" to "A0010DB",
                "matchedNomsNumber" to "A0010DB",
                "nomsNumberChanged" to "false",
                "existingBookingNumber" to "00001A",
                "matchedBookingNumber" to "00001A",
                "bookingNumberChanged" to "false",
                "custody" to "${custodyId("A000010")}",
                "sentenceDateInDelius" to "2022-11-11",
                "sentenceDateInNomis" to "2022-11-11",
                "totalCustodialEvents" to "1",
                "matchingCustodialEvents" to "1"
            )
        )
    }

    @Test
    @Order(3)
    fun `prisoner received updated identifiers noms number match (HMPPS_KEY)`() {
        withMatchResponse("probation-search-single-result-db-compare.json")
        withJsonResponse(
            "/prison-api/api/bookings/offenderNo/A0001DB",
            "prison-api-A0001DB-booking.json"
        )
        withJsonResponse(
            "/prison-api/api/prisoners/A0010DB",
            "prison-api-A0010DB-prisoner-match-noms.json"
        )

        val event = prepEvent("prisoner-status-changed-db", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        val eventCaptor = argumentCaptor<Map<String, String?>>()
        verify(telemetryService).trackEvent(
            org.mockito.kotlin.eq("MatchResultSuccess"),
            eventCaptor.capture(),
            anyMap()
        )
        assertThat(eventCaptor.firstValue["matchedBy"], equalTo("HMPPS_KEY"))
    }

    @Test
    @Order(4)
    fun `prisoner received updated identifiers cro number match (EXTERNAL_KEY)`() {
        withMatchResponse("probation-search-single-result-db-compare.json")
        withJsonResponse(
            "/prison-api/api/bookings/offenderNo/A0100DB",
            "prison-api-A0100DB-booking.json"
        )
        withJsonResponse(
            "/prison-api/api/prisoners/A0100DB",
            "prison-api-A0100DB-prisoner-match-cro.json"
        )

        val event = prepEvent("prisoner-status-changed-db-cro", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        val eventCaptor = argumentCaptor<Map<String, String?>>()
        verify(telemetryService).trackEvent(
            org.mockito.kotlin.eq("MatchResultSuccess"),
            eventCaptor.capture(),
            anyMap()
        )
        assertThat(eventCaptor.firstValue["matchedBy"], equalTo("EXTERNAL_KEY"))
    }

    @Test
    @Order(5)
    fun `prisoner received updated identifiers pnc (short format) number match (EXTERNAL_KEY)`() {
        withMatchResponse("probation-search-single-result-db-compare.json")
        withJsonResponse(
            "/prison-api/api/bookings/offenderNo/A0103DB",
            "prison-api-A0103DB-booking.json"
        )
        withJsonResponse(
            "/prison-api/api/prisoners/A0103DB",
            "prison-api-A0103DB-prisoner-match-pnc-short.json"
        )

        val event = prepEvent("prisoner-status-changed-db-pnc-short", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        val eventCaptor = argumentCaptor<Map<String, String?>>()
        verify(telemetryService).trackEvent(
            org.mockito.kotlin.eq("MatchResultSuccess"),
            eventCaptor.capture(),
            anyMap()
        )
        assertThat(eventCaptor.firstValue["matchedBy"], equalTo("EXTERNAL_KEY"))
    }

    @Test
    @Order(6)
    fun `prisoner received updated identifiers pnc (long format) number match (EXTERNAL_KEY)`() {
        withMatchResponse("probation-search-single-result-db-compare.json")
        withJsonResponse(
            "/prison-api/api/bookings/offenderNo/A0104DB",
            "prison-api-A0104DB-booking.json"
        )
        withJsonResponse(
            "/prison-api/api/prisoners/A0104DB",
            "prison-api-A0104DB-prisoner-match-pnc-long.json"
        )

        val event = prepEvent("prisoner-status-changed-db-pnc-long", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        val eventCaptor = argumentCaptor<Map<String, String?>>()
        verify(telemetryService).trackEvent(
            org.mockito.kotlin.eq("MatchResultSuccess"),
            eventCaptor.capture(),
            anyMap()
        )
        assertThat(eventCaptor.firstValue["matchedBy"], equalTo("EXTERNAL_KEY"))
    }

    @Test
    @Order(7)
    fun `prisoner received updated identifiers name match (NAME)`() {
        withMatchResponse("probation-search-single-result-db-compare.json")
        withJsonResponse(
            "/prison-api/api/bookings/offenderNo/A0105DB",
            "prison-api-A0105DB-booking.json"
        )
        withJsonResponse(
            "/prison-api/api/prisoners/A0105DB",
            "prison-api-A0105DB-prisoner-match-name.json"
        )

        val event = prepEvent("prisoner-status-changed-db-name", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        val eventCaptor = argumentCaptor<Map<String, String?>>()
        verify(telemetryService).trackEvent(
            org.mockito.kotlin.eq("MatchResultSuccess"),
            eventCaptor.capture(),
            anyMap()
        )
        assertThat(eventCaptor.firstValue["matchedBy"], equalTo("NAME"))
    }

    @Test
    @Order(8)
    fun `prisoner received updated identifiers db partial name match`() {
        withMatchResponse("probation-search-single-result-db-compare.json")
        withJsonResponse(
            "/prison-api/api/bookings/offenderNo/A0101DB",
            "prison-api-A0101DB-booking.json"
        )
        withJsonResponse(
            "/prison-api/api/prisoners/A0101DB",
            "prison-api-A0101DB-prisoner-partial-name.json"
        )

        val event = prepEvent("prisoner-status-changed-db-partial-name", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        val eventCaptor = argumentCaptor<Map<String, String?>>()
        verify(telemetryService).trackEvent(
            org.mockito.kotlin.eq("MatchResultSuccess"),
            eventCaptor.capture(),
            anyMap()
        )
        assertThat(eventCaptor.firstValue["matchedBy"], equalTo("PARTIAL_NAME"))
    }

    @Test
    @Order(9)
    fun `prisoner received updated identifiers db partial name match lenient dob`() {
        withMatchResponse("probation-search-single-result-db-compare.json")
        withJsonResponse(
            "/prison-api/api/bookings/offenderNo/A0102DB",
            "prison-api-A0102DB-booking.json"
        )
        withJsonResponse(
            "/prison-api/api/prisoners/A0102DB",
            "prison-api-A0102DB-prisoner-partial-name-lenient-dob.json"
        )

        val event = prepEvent("prisoner-status-changed-db-lenient-dob", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        val eventCaptor = argumentCaptor<Map<String, String?>>()
        verify(telemetryService).trackEvent(
            org.mockito.kotlin.eq("MatchResultSuccess"),
            eventCaptor.capture(),
            anyMap()
        )
        assertThat(eventCaptor.firstValue["matchedBy"], equalTo("PARTIAL_NAME_DOB_LENIENT"))
    }

    @Test
    @Order(10)
    fun `prisoner received updated identifiers db no match (NOTHING)`() {
        withMatchResponse("probation-search-single-result-db-compare.json")
        withJsonResponse(
            "/prison-api/api/bookings/offenderNo/A0106DB",
            "prison-api-A0106DB-booking.json"
        )
        withJsonResponse(
            "/prison-api/api/prisoners/A0106DB",
            "prison-api-A0106DB-prisoner-no-match.json"
        )

        val event = prepEvent("prisoner-status-changed-db-no-match", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        val eventCaptor = argumentCaptor<Map<String, String?>>()
        verify(telemetryService).trackEvent(
            org.mockito.kotlin.eq("MatchResultNoMatch"),
            eventCaptor.capture(),
            anyMap()
        )
        assertThat(eventCaptor.firstValue["matchedBy"], equalTo("NOTHING"))
    }

    @Test
    @Order(11)
    fun `prisoner received updated identifiers compare with api response`() {

        whenever(featureFlags.enabled("prison-identifiers-use-db-search")).thenReturn(false)
        whenever(featureFlags.enabled("prison-identifiers-compare-with-db-search")).thenReturn(true)

        withMatchResponse("probation-search-single-result-db-difference.json")
        withJsonResponse(
            "/prison-api/api/bookings/offenderNo/A0102DB",
            "prison-api-A0102DB-booking.json"
        )
        withJsonResponse(
            "/prison-api/api/prisoners/A0102DB",
            "prison-api-A0102DB-prisoner-partial-name-lenient-dob.json"
        )

        val event = prepEvent("prisoner-status-changed-db-lenient-dob", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        val eventCaptorComparisonFailure = argumentCaptor<Map<String, String?>>()

        verify(telemetryService).trackEvent(
            org.mockito.kotlin.eq("MatchDifferenceFound"),
            eventCaptorComparisonFailure.capture(),
            anyMap()
        )

        assertThat(
            eventCaptorComparisonFailure.firstValue, equalTo(
                mapOf(
                    "nomsNumber" to "A0102DB",
                    "apiMatches" to "A000020",
                    "dbMatches" to "A000010"
                )
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
