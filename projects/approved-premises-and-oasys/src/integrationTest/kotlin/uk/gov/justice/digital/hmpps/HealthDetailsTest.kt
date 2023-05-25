package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.model.HealthDetail
import uk.gov.justice.digital.hmpps.model.HealthDetails
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class HealthDetailsTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return health details`() {
        val result = mockMvc
            .perform(get("/health-details/D006296").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful)
            .andReturn()

        val healthDetails = objectMapper.readValue(result.response.contentAsString, HealthDetails::class.java)
        assertThat(healthDetails.initiationDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-09T14:33:53Z").withZoneSameInstant(EuropeLondon))
        assertThat(healthDetails.lastUpdatedDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-17T15:02:17Z").withZoneSameInstant(EuropeLondon))

        assertThat(healthDetails.health.generalHealth).isTrue
        assertThat(healthDetails.health.alcoholMisuse).isEqualTo(
            HealthDetail(
                "Alcohol misuse - Community",
                "Alcohol misuse - Electronic Monitoring",
                "Alcohol misuse - Programme"
            )
        )
        assertThat(healthDetails.health.needForInterpreter).isEqualTo(
            HealthDetail(
                "Need for interpreter - Community",
                "Need for interpreter - Electronic Monitoring",
                "Need for interpreter - Programme"
            )
        )
    }

    @Test
    fun `should return HTTP not found when CRN does not exist`() {
        mockMvc
            .perform(get("/health-details/D000001").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
    }
}
