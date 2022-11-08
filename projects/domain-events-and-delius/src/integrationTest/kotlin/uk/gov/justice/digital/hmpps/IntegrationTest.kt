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
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.model.BreachDetails
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `get breach details returns ok`() {
        val result = mockMvc
            .perform(get("/details/enforcement.breach.raised/${NsiGenerator.DEFAULT_NSI.id}").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andReturn()

        val breachDetails = objectMapper.readValue(result.response.contentAsString, BreachDetails::class.java)
        assertThat(breachDetails.crn).isEqualTo(NsiGenerator.DEFAULT_NSI.offender.crn)
        assertThat(breachDetails.eventNumber).isEqualTo(NsiGenerator.DEFAULT_NSI.event!!.number)
    }

    @Test
    fun `get breach details returns not found when NSI id does not exist in Delius`() {
        mockMvc
            .perform(get("/details/enforcement.breach.raised/000001").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
    }
}
