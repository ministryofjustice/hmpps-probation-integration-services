package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class NomsNumberIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @SpyBean
    lateinit var personRepository: PersonRepository

    @SpyBean
    lateinit var custodyRepository: CustodyRepository

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    @Order(1)
    fun `API call retuns not found in delius`() {
        val crn = "ZZZ"

        mockMvc
            .perform(post("/person/populate-noms-number").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)

        verify(telemetryService, never()).trackEvent(any(), any(), any())
    }

    @Test
    @Order(2)
    fun `API call retuns Noms number already in delius`() {
        val crn = PersonGenerator.PERSON_WITH_NOMS.crn

        mockMvc
            .perform(post("/person/populate-noms-number").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)

        val nameCapture = argumentCaptor<String>()
        val propertyCaptor = argumentCaptor<Map<String, String>>()
        verify(telemetryService, timeout(5000)).trackEvent(nameCapture.capture(), propertyCaptor.capture(), any())

        assertThat(nameCapture.firstValue, equalTo("SuccessfulMatch"))
        assertThat(
            propertyCaptor.firstValue, equalTo(
                mapOf(
                    "crn" to "A000001",
                    "existingNomsId" to "E1234XS",
                    "custodialEvents" to "1",
                    "matchedNomsId" to "E1234XS",
                    "matchedBookingNumber" to "76543A",
                    "dryRun" to "true"
                )
            )
        )
    }

    @Test
    @Order(3)
    fun `API call retuns single match via prison search api`() {
        val crn = PersonGenerator.PERSON_WITH_NO_NOMS.crn

        mockMvc
            .perform(post("/person/populate-noms-number?dryRun=false").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)

        val nameCapture = argumentCaptor<String>()
        val propertyCaptor = argumentCaptor<Map<String, String>>()
        verify(telemetryService, timeout(5000)).trackEvent(nameCapture.capture(), propertyCaptor.capture(), any())

        assertThat(nameCapture.firstValue, equalTo("SuccessfulMatch"))
        assertThat(
            propertyCaptor.firstValue, equalTo(
                mapOf(
                    "crn" to "A000002",
                    "custodialEvents" to "1",
                    "matchedNomsId" to "G5541UN",
                    "matchedBookingNumber" to "13831A",
                    "matchedSentenceDate" to "2022-12-12",
                    "sentenceDate" to "2022-12-12",
                    "dryRun" to "false"
                )
            )
        )
    }

    @Test
    @Order(4)
    fun `API call retuns a single match from multiple matches found in prison search api does not update delius`() {
        val crn = PersonGenerator.PERSON_WITH_MULTI_MATCH.crn

        mockMvc
            .perform(post("/person/populate-noms-number").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)

        val nameCapture = argumentCaptor<String>()
        val propertyCaptor = argumentCaptor<Map<String, String>>()
        verify(telemetryService, timeout(5000)).trackEvent(nameCapture.capture(), propertyCaptor.capture(), any())

        assertThat(nameCapture.firstValue, equalTo("SuccessfulMatch"))
        assertThat(
            propertyCaptor.firstValue, equalTo(
                mapOf(
                    "crn" to "A000003",
                    "custodialEvents" to "1",
                    "matchedNomsId" to "G5541WW",
                    "matchedBookingNumber" to "13831A",
                    "matchedSentenceDate" to "2022-12-12",
                    "sentenceDate" to "2022-12-12",
                    "dryRun" to "true"
                )
            )
        )
    }

    @Test
    @Order(5)
    fun `API call retuns a single match from multiple matches found in prison search api updated person in delius`() {
        val crn = PersonGenerator.PERSON_WITH_MULTI_MATCH.crn
        val custodyId = personRepository.findSentencedByCrn(crn).first().custody!!.id

        mockMvc
            .perform(post("/person/populate-noms-number?dryRun=false").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)

        val nameCapture = argumentCaptor<String>()
        val propertyCaptor = argumentCaptor<Map<String, String>>()
        verify(telemetryService, timeout(5000)).trackEvent(nameCapture.capture(), propertyCaptor.capture(), any())

        assertThat(nameCapture.firstValue, equalTo("SuccessfulMatch"))
        assertThat(
            propertyCaptor.firstValue, equalTo(
                mapOf(
                    "crn" to "A000003",
                    "custodialEvents" to "1",
                    "matchedNomsId" to "G5541WW",
                    "matchedBookingNumber" to "13831A",
                    "matchedSentenceDate" to "2022-12-12",
                    "sentenceDate" to "2022-12-12",
                    "dryRun" to "false"
                )
            )
        )

        val custody = custodyRepository.findByIdOrNull(custodyId)
        assertThat(custody?.bookingRef, equalTo("13831A"))
    }

    @Test
    @Order(6)
    fun `API call cant determine a match from multiple matches found in prison search api`() {
        val crn = PersonGenerator.PERSON_WITH_NO_MATCH.crn

        mockMvc
            .perform(post("/person/populate-noms-number").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)

        val nameCapture = argumentCaptor<String>()
        val propertyCaptor = argumentCaptor<Map<String, String>>()
        verify(telemetryService, timeout(5000)).trackEvent(nameCapture.capture(), propertyCaptor.capture(), any())

        assertThat(nameCapture.firstValue, equalTo("UnsuccessfulMatch"))
        assertThat(
            propertyCaptor.firstValue, equalTo(
                mapOf(
                    "crn" to "A000004",
                    "custodialEvents" to "1",
                    "sentenceDates" to "2022-12-12",
                    "G5541WW" to "DateOfBirth:INCONCLUSIVE",
                    "A1234YZ" to "Name:PARTIAL, DateOfBirth:INCONCLUSIVE",
                    "dryRun" to "true"
                )
            )
        )
    }

    @Test
    @Order(7)
    fun `API call retuns single match but noms number already in delius via prison search api`() {
        val crn = PersonGenerator.PERSON_WITH_NOMS_IN_DELIUS.crn

        mockMvc
            .perform(post("/person/populate-noms-number").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)

        val nameCapture = argumentCaptor<String>()
        val propertyCaptor = argumentCaptor<Map<String, String>>()
        verify(telemetryService, timeout(5000)).trackEvent(nameCapture.capture(), propertyCaptor.capture(), any())

        assertThat(nameCapture.firstValue, equalTo("SuccessfulMatch"))
        assertThat(
            propertyCaptor.firstValue, equalTo(
                mapOf(
                    "crn" to "A000005",
                    "custodialEvents" to "1",
                    "matchedNomsId" to "G5541UN",
                    "matchedBookingNumber" to "13831A",
                    "matchedSentenceDate" to "2022-12-12",
                    "sentenceDate" to "2022-12-12",
                    "dryRun" to "true"
                )
            )
        )
    }
}
