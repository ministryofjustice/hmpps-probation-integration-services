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
import uk.gov.justice.digital.hmpps.controller.toModel
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.NSIGenerator
import uk.gov.justice.digital.hmpps.data.generator.NSIStatusGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.model.DutyToReferNSI
import uk.gov.justice.digital.hmpps.model.Officer
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var wireMockServer: WireMockServer

    @MockBean lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `API call retuns a success response`() {
        val result = mockMvc
            .perform(get("/duty-to-refer-nsi/X123123?type=CRN").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful).andReturn()

        val detailResponse = objectMapper.readValue(result.response.contentAsString, DutyToReferNSI::class.java)
        Assertions.assertThat(detailResponse).isEqualTo(getNSI())
    }

    private fun getNSI() = DutyToReferNSI(
        ReferenceDataGenerator.DTR_SUB_TYPE.description,
        NSIGenerator.DEFAULT.referralDate,
        ProviderGenerator.DEFAULT_AREA.description,
        ProviderGenerator.DEFAULT_TEAM.description,
        Officer(ProviderGenerator.DEFAULT_STAFF.forename, ProviderGenerator.DEFAULT_STAFF.surname, ProviderGenerator.DEFAULT_STAFF.middleName),
        NSIStatusGenerator.INITIATED.description,
        NSIGenerator.DEFAULT.actualStartDate,
        NSIGenerator.DEFAULT.notes,
        AddressGenerator.DEFAULT.toModel()
    )

    @Test
    fun `API call retuns a 404 response`() {
        mockMvc
            .perform(get("/duty-to-refer-nsi/N123123B?type=CRN").withOAuth2Token(wireMockServer))
            .andExpect(status().is4xxClientError)
    }
}
