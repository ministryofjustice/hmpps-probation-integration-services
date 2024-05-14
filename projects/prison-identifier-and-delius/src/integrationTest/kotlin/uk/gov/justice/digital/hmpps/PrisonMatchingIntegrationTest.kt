package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
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
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_WITH_DUPLICATE_NOMS
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class PrisonMatchingIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @SpyBean
    lateinit var personRepository: PersonRepository

    @SpyBean
    lateinit var custodyRepository: CustodyRepository

    @SpyBean
    lateinit var additionalIdentifierRepository: AdditionalIdentifierRepository

    @SpyBean
    lateinit var contactRepository: ContactRepository

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    @Order(1)
    fun `crn does not exist`() {
        val crn = "ZZZ"

        mockMvc
            .perform(post("/person/match-by-crn").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)

        verify(telemetryService, never()).trackEvent(any(), any(), any())
    }

    @Test
    @Order(2)
    fun `no match with existing noms number if no custodial sentence in Delius`() {
        val crn = PersonGenerator.PERSON_WITH_NOMS.crn

        mockMvc
            .perform(post("/person/match-by-crn").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)

        verify(telemetryService, timeout(5000)).trackEvent(
            "MatchResultNoMatch", mapOf(
                "reason" to "No single match found in prison system",
                "crn" to "A000001",
                "potentialMatches" to """[{"nomsNumber":"E1234XS"}]""",
                "dryRun" to "true"
            )
        )
    }

    @Test
    @Order(3)
    fun `single match with no existing identifiers`() {
        val crn = PersonGenerator.PERSON_WITH_NO_NOMS.crn
        val custodyId = personRepository.findSentencedByCrn(crn).first().custody!!.id

        mockMvc
            .perform(post("/person/match-by-crn?dryRun=false").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)

        verify(telemetryService, timeout(5000)).trackEvent(
            "MatchResultSuccess", mapOf(
                "reason" to "Matched CRN A000002 to NOMS number G5541UN and custody $custodyId to 13831A",
                "crn" to "A000002",
                "potentialMatches" to """[{"nomsNumber":"G5541UN"}]""",
                "matchedNomsNumber" to "G5541UN",
                "nomsNumberChanged" to "true",
                "matchedBookingNumber" to "13831A",
                "custody" to "$custodyId",
                "bookingNumberChanged" to "true",
                "sentenceDateInDelius" to "2022-12-12",
                "sentenceDateInNomis" to "2022-12-12",
                "totalCustodialEvents" to "1",
                "matchingCustodialEvents" to "1",
                "dryRun" to "false"
            )
        )

        // also removes any duplicates
        val duplicate = personRepository.getByCrn(PERSON_WITH_DUPLICATE_NOMS.crn)
        assertThat(duplicate.nomsNumber, nullValue())
        val identifiers = additionalIdentifierRepository.findAll().filter { it.personId == duplicate.id }
            .associate { it.type.code to it.identifier }
        assertThat(identifiers, equalTo(mapOf("DNOMS" to "G5541UN")))
    }

    @Test
    @Order(4)
    fun `multiple potential matches from search, but one exact match - dry run does not update Delius`() {
        val crn = PersonGenerator.PERSON_WITH_MULTI_MATCH.crn
        val custodyId = personRepository.findSentencedByCrn(crn).first().custody!!.id

        mockMvc
            .perform(post("/person/match-by-crn").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)

        verify(telemetryService, timeout(5000)).trackEvent(
            "MatchResultSuccess", mapOf(
                "reason" to "Matched CRN A000003 to NOMS number G5541WW and custody $custodyId to 13831A",
                "crn" to "A000003",
                "potentialMatches" to """[{"nomsNumber":"G5541WW"},{"nomsNumber":"A1234YZ","Name":"PARTIAL","SentenceDate":"INCONCLUSIVE"}]""",
                "matchedNomsNumber" to "G5541WW",
                "nomsNumberChanged" to "true",
                "matchedBookingNumber" to "13831A",
                "bookingNumberChanged" to "true",
                "custody" to "$custodyId",
                "sentenceDateInDelius" to "2022-12-12",
                "sentenceDateInNomis" to "2022-12-12",
                "totalCustodialEvents" to "1",
                "matchingCustodialEvents" to "1",
                "dryRun" to "true"
            )
        )

        verify(personRepository, never()).save(any())
        verify(custodyRepository, never()).save(any())
        verify(additionalIdentifierRepository, never()).save(any())
    }

    @Test
    @Order(5)
    fun `multiple potential matches from search, but one exact match - no dry run`() {
        val crn = PersonGenerator.PERSON_WITH_MULTI_MATCH.crn
        val custodyId = personRepository.findSentencedByCrn(crn).first().custody!!.id

        mockMvc
            .perform(post("/person/match-by-crn?dryRun=false").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)

        verify(telemetryService, timeout(5000)).trackEvent(
            "MatchResultSuccess", mapOf(
                "reason" to "Matched CRN A000003 to NOMS number G5541WW and custody $custodyId to 13831A",
                "crn" to "A000003",
                "potentialMatches" to """[{"nomsNumber":"G5541WW"},{"nomsNumber":"A1234YZ","Name":"PARTIAL","SentenceDate":"INCONCLUSIVE"}]""",
                "matchedNomsNumber" to "G5541WW",
                "nomsNumberChanged" to "true",
                "matchedBookingNumber" to "13831A",
                "bookingNumberChanged" to "true",
                "custody" to "$custodyId",
                "sentenceDateInDelius" to "2022-12-12",
                "sentenceDateInNomis" to "2022-12-12",
                "totalCustodialEvents" to "1",
                "matchingCustodialEvents" to "1",
                "dryRun" to "false"
            )
        )

        val person = personRepository.getByCrn(crn)
        assertThat(person.nomsNumber, equalTo("G5541WW"))
        val custody = custodyRepository.findByIdOrNull(custodyId)!!
        assertThat(custody.prisonerNumber, equalTo("13831A"))
        verify(contactRepository).save(check {
            assertThat(it.personId, equalTo(person.id))
            assertThat(it.eventId, equalTo(custody.disposal.event.id))
            assertThat(it.type.code, equalTo("EDSS"))
            assertThat(it.notes, equalTo("Prison Number: 13831A" + System.lineSeparator()))
        })
    }

    @Test
    @Order(6)
    fun `no match - nothing updated`() {
        val crn = PersonGenerator.PERSON_WITH_NO_MATCH.crn

        mockMvc
            .perform(post("/person/match-by-crn").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)

        verify(telemetryService, timeout(5000)).trackEvent(
            "MatchResultNoMatch", mapOf(
                "reason" to "No single match found in prison system",
                "crn" to "A000004",
                "potentialMatches" to """[{"nomsNumber":"G5541WW","DateOfBirth":"INCONCLUSIVE"},{"nomsNumber":"A1234YZ","Name":"PARTIAL","DateOfBirth":"INCONCLUSIVE"}]""",
                "dryRun" to "true"
            )
        )

        verify(personRepository, never()).save(any())
        verify(custodyRepository, never()).save(any())
        verify(additionalIdentifierRepository, never()).save(any())
    }

    @Test
    @Order(7)
    fun `API call retuns single match but noms number already in delius via prison search api`() {
        val crn = PersonGenerator.PERSON_WITH_NOMS_IN_DELIUS.crn
        val custodyId = personRepository.findSentencedByCrn(crn).first().custody!!.id

        mockMvc
            .perform(post("/person/match-by-crn").withToken().withJson(listOf(crn)))
            .andExpect(status().is2xxSuccessful)

        verify(telemetryService, timeout(5000)).trackEvent(
            "MatchResultSuccess", mapOf(
                "reason" to "Matched CRN A000005 to NOMS number G5541UN and custody $custodyId to 13831A",
                "crn" to "A000005",
                "potentialMatches" to """[{"nomsNumber":"G5541UN"}]""",
                "matchedNomsNumber" to "G5541UN",
                "nomsNumberChanged" to "true",
                "matchedBookingNumber" to "13831A",
                "bookingNumberChanged" to "true",
                "custody" to "$custodyId",
                "sentenceDateInDelius" to "2022-12-12",
                "sentenceDateInNomis" to "2022-12-12",
                "totalCustodialEvents" to "1",
                "matchingCustodialEvents" to "1",
                "dryRun" to "true"
            )
        )
    }
}
