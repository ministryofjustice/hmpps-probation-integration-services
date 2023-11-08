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
import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator
import uk.gov.justice.digital.hmpps.data.generator.KeyDateGenerator
import uk.gov.justice.digital.hmpps.data.generator.NSIGenerator
import uk.gov.justice.digital.hmpps.model.Detail
import uk.gov.justice.digital.hmpps.model.KeyDate
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.name
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class DetailsIntegrationTest {
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
        val noms = DetailsGenerator.PERSON.nomsNumber
        val result = mockMvc
            .perform(get("/detail/$noms?type=NOMS").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful).andReturn()

        val detailResponse = objectMapper.readValue(result.response.contentAsString, Detail::class.java)
        Assertions.assertThat(detailResponse).isEqualTo(getDetail())
    }

    @Test
    fun `API call retuns a success response using CRN`() {
        val crn = DetailsGenerator.PERSON.crn
        val result = mockMvc
            .perform(get("/detail/$crn?type=CRN").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful).andReturn()

        val detailResponse = objectMapper.readValue(result.response.contentAsString, Detail::class.java)
        Assertions.assertThat(detailResponse).isEqualTo(getDetail())
    }

    private fun getDetail(): Detail = Detail(
        DetailsGenerator.PERSON.name(),
        DetailsGenerator.PERSON.dateOfBirth,
        DetailsGenerator.PERSON.crn,
        DetailsGenerator.PERSON.nomsNumber,
        DetailsGenerator.PERSON.pncNumber,
        DetailsGenerator.DISTRICT.description,
        DetailsGenerator.DEFAULT_PA.description,
        Name(DetailsGenerator.STAFF.forename, DetailsGenerator.STAFF.middleName, DetailsGenerator.STAFF.surname),
        ConvictionEventGenerator.OFFENCE_MAIN.description,
        DetailsGenerator.PERSON.religion?.description,
        listOf(KeyDate(KeyDateGenerator.SED_KEYDATE.code, KeyDateGenerator.SED_KEYDATE.description, KeyDateGenerator.KEYDATE.date)),
        DetailsGenerator.RELEASE.date,
        DetailsGenerator.RELEASE.releaseType.description,
        DetailsGenerator.INSTITUTION.name,
        DetailsGenerator.RECALL.date,
        DetailsGenerator.RECALL.reason.description,
        NSIGenerator.RECALL_NSI.referralDate,
        NSIGenerator.BREACH_NSI.referralDate
    )
}
