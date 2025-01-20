package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.overview.Rar
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import uk.gov.justice.digital.hmpps.api.model.sentence.Requirement
import uk.gov.justice.digital.hmpps.api.model.sentence.RequirementNoteDetail
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REQUIREMENT
import uk.gov.justice.digital.hmpps.service.toSummary
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RequirementIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/requirement/1/note/1"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `requirement not found`() {
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/requirement/0/note/6")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<RequirementNoteDetail>()

        val expected = RequirementNoteDetail(PersonGenerator.OVERVIEW.toSummary())

        assertEquals(expected, response)
    }

    @Test
    fun `get requirement`() {
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/requirement/${REQUIREMENT.id}/note/0")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<RequirementNoteDetail>()

        val expected = RequirementNoteDetail(
            PersonGenerator.OVERVIEW.toSummary(),
            Requirement(
                REQUIREMENT.id,
                "F",
                LocalDate.now().minusDays(1),
                LocalDate.now(),
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(3),
                null,
                "2 days RAR, 1 completed",
                12,
                null,
                requirementNote = NoteDetail(0, note = "my notes"),
                rar = Rar(completed = 1, scheduled = 1, nsiCompleted = 0, totalDays = 2)
            )
        )

        assertEquals(expected, response)
    }
}