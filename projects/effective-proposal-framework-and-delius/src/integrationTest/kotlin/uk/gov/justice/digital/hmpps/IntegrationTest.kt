package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.epf.CaseDetails
import uk.gov.justice.digital.hmpps.epf.Court
import uk.gov.justice.digital.hmpps.epf.Name
import uk.gov.justice.digital.hmpps.epf.Provider
import uk.gov.justice.digital.hmpps.epf.Sentence
import uk.gov.justice.digital.hmpps.epf.entity.Person
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
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
        val person = PersonGenerator.DEFAULT
        val crn = person.crn
        val eventNumber = 1
        val result = mockMvc
            .perform(get("/case-details/$crn/$eventNumber").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful).andReturn()
        val detailResponse = objectMapper.readValue(result.response.contentAsString, CaseDetails::class.java)

        MatcherAssert.assertThat(detailResponse, equalTo(getDetailResponse()))
    }

    @ParameterizedTest
    @MethodSource("limitedAccess")
    fun `Response of Not Found when case is Restricted Or Excluded`(person: Person) {
        mockMvc
            .perform(get("/case-details/${person.crn}/1").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message", equalTo("Person with crn of ${person.crn} not found")))
    }

    private fun getDetailResponse(): CaseDetails {
        return CaseDetails(
            Name(
                PersonGenerator.DEFAULT.forename,
                PersonGenerator.DEFAULT.secondName,
                PersonGenerator.DEFAULT.surname
            ),
            PersonGenerator.DEFAULT.dateOfBirth,
            PersonGenerator.DEFAULT.gender.description,
            Sentence(SentenceGenerator.DEFAULT_SENTENCE.date, Court(SentenceGenerator.DEFAULT_COURT.name), null),
            Provider(ProviderGenerator.DEFAULT.code, ProviderGenerator.DEFAULT.description)
        )
    }

    companion object {
        @JvmStatic
        fun limitedAccess() = listOf(PersonGenerator.EXCLUDED, PersonGenerator.RESTRICTED)
    }
}
