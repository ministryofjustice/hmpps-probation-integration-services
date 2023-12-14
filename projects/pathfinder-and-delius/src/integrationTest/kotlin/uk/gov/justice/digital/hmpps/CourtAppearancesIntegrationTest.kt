package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator
import uk.gov.justice.digital.hmpps.model.BatchRequest
import uk.gov.justice.digital.hmpps.model.CourtAppearance
import uk.gov.justice.digital.hmpps.model.CourtAppearancesContainer
import uk.gov.justice.digital.hmpps.model.Type
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CourtAppearancesIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `API call retuns a success response`() {
        val crns = listOf(CourtAppearanceGenerator.DEFAULT_PERSON.crn)
        val result = mockMvc
            .perform(
                post("/court-appearances").withOAuth2Token(wireMockServer)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            BatchRequest(
                                crns
                            )
                        )
                    )
            )
            .andExpect(status().is2xxSuccessful).andReturn()

        val detailResponse = objectMapper.readValue<CourtAppearancesContainer>(result.response.contentAsString)
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
