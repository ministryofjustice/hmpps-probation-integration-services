package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.service.toSummary
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SentenceIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `no active sentences`() {
        val response = mockMvc
            .perform(MockMvcRequestBuilders.get("/sentence/${PersonDetailsGenerator.PERSONAL_DETAILS.crn}").withToken())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<SentenceOverview>()

        val expected = SentenceOverview(
            PersonDetailsGenerator.PERSONAL_DETAILS.toSummary()
        )

        assertEquals(expected, response)
    }

    @Test
    fun `get active sentences`() {
        val response = mockMvc
            .perform(MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}").withToken())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<SentenceOverview>()

        val expected = SentenceOverview(
            PersonGenerator.OVERVIEW.toSummary(),
            listOf(
                SentenceSummary(1234567, "Pre-Sentence"),
                SentenceSummary(7654321, "Default Sentence Type")
            ),

            Sentence(
                OffenceDetails(
                    "1234567",
                    Offence("Another Murder", 1),
                    LocalDate.now(),
                    "overview",
                    emptyList()
                ),
                Conviction(null, null, null, listOf()),
                null,
                listOf(),
                listOf(),
                null,
                listOf()
            )
        )

        assertEquals(expected, response)
    }

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/sentence/X123456"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }
}