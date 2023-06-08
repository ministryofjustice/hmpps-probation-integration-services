package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ConvictionEventGenerator
import uk.gov.justice.digital.hmpps.model.BatchRequest
import uk.gov.justice.digital.hmpps.model.Conviction
import uk.gov.justice.digital.hmpps.model.ConvictionsContainer
import uk.gov.justice.digital.hmpps.model.Offence
import uk.gov.justice.digital.hmpps.model.PersonConviction
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ConvictionsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `API call retuns a success response using CRN`() {
        val crns = listOf(ConvictionEventGenerator.PERSON.crn)
        val result = mockMvc
            .perform(
                MockMvcRequestBuilders.post("/convictions").withOAuth2Token(wireMockServer)
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

        val detailResponse = objectMapper.readValue(result.response.contentAsString, ConvictionsContainer::class.java)
        Assertions.assertThat(detailResponse).isEqualTo(getConvictions())
    }

    private fun getConvictions(): ConvictionsContainer = ConvictionsContainer(
        listOf(
            PersonConviction(
                ConvictionEventGenerator.DEFAULT_EVENT.convictionEventPerson.crn,
                listOf(
                    Conviction(
                        ConvictionEventGenerator.DEFAULT_EVENT.convictionDate,
                        ConvictionEventGenerator.DISPOSAL_TYPE.description,
                        listOf(
                            Offence(ConvictionEventGenerator.OFFENCE_MAIN.description, true),
                            Offence(ConvictionEventGenerator.OFFENCE_OTHER.description, false)
                        )
                    )
                )
            )
        )
    )
}
