package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.model.NeedsDetails
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NeedsDetailTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return needs details`() {
        val result = mockMvc
            .perform(MockMvcRequestBuilders.get("/needs-details/D006296").withOAuth2Token(wireMockServer))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn()

        val needsDetails = objectMapper.readValue(result.response.contentAsString, NeedsDetails::class.java)
        assertThat(needsDetails.initiationDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-09T14:33:53Z").withZoneSameInstant(EuropeLondon))
        assertThat(needsDetails.lastUpdatedDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-17T15:02:17Z").withZoneSameInstant(EuropeLondon))
        assertThat(needsDetails.needs?.financeIssuesDetails)
            .isEqualTo("no money")
        assertThat(needsDetails.needs?.attitudeIssuesDetails)
            .isEqualTo("likes to behave badly")
        assertThat(needsDetails.dateCompleted).isNull()
        assertThat(needsDetails.linksToHarm?.alcoholLinkedToHarm).isFalse
        assertThat(needsDetails.linksToHarm?.drugLinkedToHarm).isFalse
        assertThat(needsDetails.linksToHarm?.educationTrainingEmploymentLinkedToHarm).isTrue
    }

    @Test
    fun `should return HTTP not found when CRN does not exist`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/needs-details/D000001").withOAuth2Token(wireMockServer))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }
}
