package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.model.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.model.ProbationArea
import uk.gov.justice.digital.hmpps.model.ProbationAreaContainer
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ProbationAreaIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `API call retuns a success response`() {
        val detailResponse = mockMvc
            .perform(get("/probation-areas").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ProbationAreaContainer>()

        Assertions.assertThat(detailResponse).isEqualTo(getProbationAreas())
    }

    private fun getProbationAreas(): ProbationAreaContainer = ProbationAreaContainer(
        listOf(
            ProbationArea(
                ProbationAreaGenerator.DEFAULT_PA.code,
                ProbationAreaGenerator.DEFAULT_PA.description,
                listOf(
                    LocalDeliveryUnit(
                        ProbationAreaGenerator.DEFAULT_LDU.code,
                        ProbationAreaGenerator.DEFAULT_LDU.description
                    )
                )
            )
        )
    )
}
