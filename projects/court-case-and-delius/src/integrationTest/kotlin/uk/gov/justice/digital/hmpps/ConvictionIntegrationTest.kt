package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.conviction.Conviction
import uk.gov.justice.digital.hmpps.api.model.conviction.Offence
import uk.gov.justice.digital.hmpps.api.model.conviction.OffenceDetail
import uk.gov.justice.digital.hmpps.api.model.conviction.Sentence
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.CURRENT_SENTENCE
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.MAIN_OFFENCE
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ConvictionIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `API call probation record not found`() {
        mockMvc
            .perform(get("/probation-case/A123456/convictions/1").withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Person with crn of A123456 not found"))
    }

    @Test
    fun `API call sentence not found`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        mockMvc
            .perform(get("/probation-case/$crn/convictions/3").withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Conviction with ID 3 for Offender with crn C123456 not found"))
    }

    @Test
    fun `API call retuns sentence and custodial status information by crn convictionId`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        val event = SentenceGenerator.CURRENTLY_MANAGED
        val mainOffence = SentenceGenerator.MAIN_OFFENCE_DEFAULT

        val expectedOffenceDetail =
            OffenceDetail(
                MAIN_OFFENCE.code,
                MAIN_OFFENCE.description,
                MAIN_OFFENCE.abbreviation,
                MAIN_OFFENCE.mainCategoryCode,
                MAIN_OFFENCE.mainCategoryDescription,
                MAIN_OFFENCE.mainCategoryAbbreviation,
                MAIN_OFFENCE.ogrsOffenceCategory.description,
                MAIN_OFFENCE.subCategoryCode,
                MAIN_OFFENCE.subCategoryDescription,
                MAIN_OFFENCE.form20Code,
                MAIN_OFFENCE.subCategoryAbbreviation,
                MAIN_OFFENCE.cjitCode
            )
        val expectedOffences = listOf(
            Offence(
                mainOffence.id,
                mainOffence = true,
                expectedOffenceDetail,
                mainOffence.date,
                mainOffence.offenceCount,
                mainOffence.tics,
                mainOffence.verdict,
                mainOffence.offenderId,
                mainOffence.created,
                mainOffence.updated
            )
        )
        val expectedSentence = Sentence(
            CURRENT_SENTENCE.id,
            CURRENT_SENTENCE.disposalType.description,
            CURRENT_SENTENCE.entryLength,
            CURRENT_SENTENCE.entryLengthUnit?.description,
            CURRENT_SENTENCE.length2,
            CURRENT_SENTENCE.entryLength2Unit?.description,
            CURRENT_SENTENCE.length,
            CURRENT_SENTENCE.effectiveLength,
            CURRENT_SENTENCE.lengthInDays,
            CURRENT_SENTENCE.enteredSentenceEndDate
        )
        val expectedResponse = Conviction(
            event.id, event.eventNumber,
            event.active,
            event.inBreach,
            2,
            event.breachEnd,
            false,
            event.convictionDate,
            event.referralDate,
            expectedOffences,
            expectedSentence
        )

        val response = mockMvc
            .perform(get("/probation-case/$crn/convictions/${event.id}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andDo(print())
            .andReturn().response.contentAsJson<Conviction>()

        assertEquals(expectedResponse, response)
    }
}