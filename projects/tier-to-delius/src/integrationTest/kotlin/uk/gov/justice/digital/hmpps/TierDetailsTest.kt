package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.CaseEntityGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class TierDetailsTest {
    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var wireMockserver: WireMockServer

    @Autowired lateinit var objectMapper: ObjectMapper

    @Test
    fun `successful response`() {
        
        mockMvc.perform(
            MockMvcRequestBuilders.get("/tier-details/F001022").withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)

        )
            .andExpect(status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.jsonPath("$.gender").value(CaseEntityGenerator.DEFAULT.gender.description))
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentTier").value(CaseEntityGenerator.DEFAULT.tier?.code))
            .andExpect(MockMvcResultMatchers.jsonPath("$.rsrscore").value(CaseEntityGenerator.DEFAULT.dynamicRsrScore))
    }
}
