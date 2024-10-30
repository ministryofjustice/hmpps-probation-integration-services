package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.sentence.LicenceCondition
import uk.gov.justice.digital.hmpps.api.model.sentence.LicenceConditionNote
import uk.gov.justice.digital.hmpps.api.model.sentence.LicenceConditionNoteDetail
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LC_WITH_NOTES
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LIC_COND_MAIN_CAT
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LIC_COND_SUB_CAT
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
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<LicenceConditionNoteDetail>()

        val expected = LicenceConditionNoteDetail(PersonGenerator.OVERVIEW.toSummary())

        assertEquals(expected, response)
    }

    @Test
    fun `note not found`() {
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/licence-condition/${LC_WITH_NOTES.id}/note/7")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<LicenceConditionNoteDetail>()

        val expected = LicenceConditionNoteDetail(
            PersonGenerator.OVERVIEW.toSummary(),
            LicenceCondition(
                LC_WITH_NOTES.id,
                LIC_COND_MAIN_CAT.description,
                LIC_COND_SUB_CAT.description,
                LocalDate.now().minusDays(7),
                LocalDate.now()
            )
        )

        assertEquals(expected, response)
    }

    @Test
    fun `get note for licence condition`() {
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/licence-condition/${LC_WITH_NOTES.id}/note/1")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<LicenceConditionNoteDetail>()

        val expected = LicenceConditionNoteDetail(
            PersonGenerator.OVERVIEW.toSummary(),
            LicenceCondition(
                LC_WITH_NOTES.id,
                LIC_COND_MAIN_CAT.description,
                LIC_COND_SUB_CAT.description,
                LocalDate.now().minusDays(7),
                LocalDate.now(),
                licenceConditionNote = LicenceConditionNote(
                    1,
                    "CVL Service",
                    LocalDate.of(2024, 4, 22),
                    """
                   ${LicenceConditionGenerator.LONG_NOTE}
                """.trimIndent()
                )
            )
        )

        assertEquals(expected, response)
    }
}