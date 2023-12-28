package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator
import uk.gov.justice.digital.hmpps.model.BatchRequest
import uk.gov.justice.digital.hmpps.model.CourtAppearance
import uk.gov.justice.digital.hmpps.model.CourtAppearancesContainer
import uk.gov.justice.digital.hmpps.model.Type
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CourtAppearancesIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `API call retuns a success response`() {
        val crns = listOf(CourtAppearanceGenerator.DEFAULT_PERSON.crn)
        val detailResponse = mockMvc
            .perform(post("/court-appearances").withToken().withJson(BatchRequest(crns)))
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<CourtAppearancesContainer>()

        Assertions.assertThat(detailResponse).isEqualTo(getCourtAppearances())
    }

    private fun getCourtAppearances(): CourtAppearancesContainer = CourtAppearancesContainer(
        listOf(
            CourtAppearance(
                CourtAppearanceGenerator.DEFAULT_CA.appearanceDate.toLocalDate(),
                Type(
                    CourtAppearanceGenerator.DEFAULT_CA_TYPE.code,
                    CourtAppearanceGenerator.DEFAULT_CA_TYPE.description
                ),
                CourtAppearanceGenerator.DEFAULT_COURT.code,
                CourtAppearanceGenerator.DEFAULT_COURT.name,
                CourtAppearanceGenerator.DEFAULT_PERSON.crn
            )
        )
    )
}
