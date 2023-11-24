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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ConvictionEventGenerator
import uk.gov.justice.digital.hmpps.data.generator.KeyDateGenerator
import uk.gov.justice.digital.hmpps.model.Conviction
import uk.gov.justice.digital.hmpps.model.ConvictionsContainer
import uk.gov.justice.digital.hmpps.model.Custody
import uk.gov.justice.digital.hmpps.model.CustodyStatus
import uk.gov.justice.digital.hmpps.model.KeyDate
import uk.gov.justice.digital.hmpps.model.Offence
import uk.gov.justice.digital.hmpps.model.Sentence
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
    fun `API call retuns a success response using NOMS`() {
        val noms = ConvictionEventGenerator.PERSON.nomsNumber
        val result = mockMvc
            .perform(get("/convictions/$noms?type=NOMS").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful).andReturn()

        val detailResponse = objectMapper.readValue(result.response.contentAsString, ConvictionsContainer::class.java)
        Assertions.assertThat(detailResponse).isEqualTo(getConvictions())
    }

    @Test
    fun `API call retuns a success response using CRN`() {
        val crn = ConvictionEventGenerator.PERSON.crn
        val result = mockMvc
            .perform(get("/convictions/$crn?type=CRN").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful).andReturn()

        val detailResponse = objectMapper.readValue(result.response.contentAsString, ConvictionsContainer::class.java)
        Assertions.assertThat(detailResponse).isEqualTo(getConvictions())
    }

    private fun getConvictions(): ConvictionsContainer = ConvictionsContainer(
        listOf(
            Conviction(
                ConvictionEventGenerator.DEFAULT_EVENT.id,
                ConvictionEventGenerator.DEFAULT_EVENT.convictionDate,
                ConvictionEventGenerator.DISPOSAL_TYPE.description,
                listOf(
                    Offence(ConvictionEventGenerator.MAIN_OFFENCE.id, ConvictionEventGenerator.OFFENCE_MAIN_TYPE.description, true),
                    Offence(ConvictionEventGenerator.OTHER_OFFENCE.id, ConvictionEventGenerator.ADDITIONAL_OFFENCE_TYPE.description, false)
                ),
                Sentence(
                    ConvictionEventGenerator.DISPOSAL.id,
                    ConvictionEventGenerator.DISPOSAL.startDate,
                    null,
                    Custody(
                        CustodyStatus(
                            KeyDateGenerator.CUSTODY.status.code,
                            KeyDateGenerator.CUSTODY.status.description
                        ),
                        listOf(
                            KeyDate(
                                KeyDateGenerator.KEYDATE.type.code,
                                KeyDateGenerator.KEYDATE.type.description,
                                KeyDateGenerator.KEYDATE.date
                            )
                        )
                    )
                )
            )
        )
    )
}
