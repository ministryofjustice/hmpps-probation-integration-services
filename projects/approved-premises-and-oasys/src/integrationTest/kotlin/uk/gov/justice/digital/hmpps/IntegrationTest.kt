package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.oasys.OasysAssessment
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `get latest layer 3 assessment`() {
        val result = mockMvc
            .perform(get("/latest-assessment/D006296").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful)
            .andReturn()

        val oasysAssessment = objectMapper.readValue(result.response.contentAsString, OasysAssessment::class.java)
        assertThat(oasysAssessment.completedDate)
            .isEqualTo(ZonedDateTime.parse("2022-07-27T12:09:41+01:00").withZoneSameInstant(EuropeLondon))
    }

    @Test
    fun `get latest layer 3 assessment not found`() {
        mockMvc
            .perform(get("/latest-assessment/D000000").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
    }
}
