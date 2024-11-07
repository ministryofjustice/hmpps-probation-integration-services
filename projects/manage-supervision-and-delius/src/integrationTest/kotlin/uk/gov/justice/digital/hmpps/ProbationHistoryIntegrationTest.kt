package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.sentence.History
import uk.gov.justice.digital.hmpps.api.model.sentence.ProbationHistory
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.service.toSummary
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.api.model.sentence.SentenceSummary

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProbationHistoryIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `no probation history`() {
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonDetailsGenerator.PERSONAL_DETAILS.crn}/probation-history")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<History>()

        val expected = History(
            PersonDetailsGenerator.PERSONAL_DETAILS.toSummary(),
            listOf(),
            ProbationHistory(0, null, 0, 0)
        )

        assertEquals(expected, response)
    }

    @Test
    fun `get probation history`() {
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/probation-history").withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<History>()

        val expected = History(
            PersonGenerator.OVERVIEW.toSummary(),
            listOf(
                SentenceSummary("1234567", "Pre-Sentence"),
                SentenceSummary("7654321", "Default Sentence Type")
            ),
            ProbationHistory(2, LocalDate.now().minusDays(7), 2, 2)
        )

        assertEquals(expected, response)
    }
}