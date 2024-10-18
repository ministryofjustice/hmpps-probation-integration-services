package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.sentence.LicenceConditionNote
import uk.gov.justice.digital.hmpps.api.model.sentence.LicenceConditionNoteDetail
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.service.toSummary
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LicenceConditionIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/licence-condition/1/note/1"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `licence condition not found`() {
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/licence-condition/1/note/6")
                    .withToken()
            )
            .andDo(print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<LicenceConditionNoteDetail>()

        val expected = LicenceConditionNoteDetail(PersonGenerator.OVERVIEW.toSummary())

        assertEquals(expected, response)
    }

    @Test
    fun `note not found`() {
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/licence-condition/${LicenceConditionGenerator.LC_WITH_NOTES.id}/note/7")
                    .withToken()
            )
            .andDo(print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<LicenceConditionNoteDetail>()

        val expected = LicenceConditionNoteDetail(PersonGenerator.OVERVIEW.toSummary())

        assertEquals(expected, response)
    }

    @Test
    fun `get note for licence condition`() {
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/licence-condition/${LicenceConditionGenerator.LC_WITH_NOTES.id}/note/1")
                    .withToken()
            )
            .andDo(print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<LicenceConditionNoteDetail>()

        val expected = LicenceConditionNoteDetail(
            PersonGenerator.OVERVIEW.toSummary(),
            LicenceConditionNote(
                1,
                "Joe Root",
                LocalDate.of(2024, 4, 23),
                """
                    You must not drink any alcohol until Wednesday 7th August 2024 unless your
                    probation officer says you can. You will need to wear an electronic tag all the time so
                    we can check this.
                """.trimIndent(),
                false
            )
        )

        assertEquals(expected, response)
    }
}