package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator
import uk.gov.justice.digital.hmpps.model.CourtAppearance
import uk.gov.justice.digital.hmpps.model.CourtAppearancesContainer
import uk.gov.justice.digital.hmpps.model.Type
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CourtAppearancesIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `API call retuns a success response`() {
        val crn = CourtAppearanceGenerator.DEFAULT_PERSON.crn
        mockMvc
            .perform(get("/court-appearances/$crn").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(getCourtAppearances())
    }

    @Test
    fun `API call retuns no results`() {
        val crn = CourtAppearanceGenerator.DEFAULT_PERSON.crn
        mockMvc
            .perform(get("/court-appearances/$crn?fromDate=2099-12-12").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(CourtAppearancesContainer(listOf()))
    }

    private fun getCourtAppearances(): CourtAppearancesContainer = CourtAppearancesContainer(
        listOf(
            CourtAppearance(
                CourtAppearanceGenerator.DEFAULT_CA.appearanceDate,
                Type(
                    CourtAppearanceGenerator.DEFAULT_CA_TYPE.code,
                    CourtAppearanceGenerator.DEFAULT_CA_TYPE.description
                ),
                CourtAppearanceGenerator.DEFAULT_COURT.code,
                CourtAppearanceGenerator.DEFAULT_COURT.name,
                CourtAppearanceGenerator.DEFAULT_PERSON.crn,
                CourtAppearanceGenerator.DEFAULT_CA.id,
                CourtAppearanceGenerator.DEFAULT_PERSON.id
            )
        )
    )
}
