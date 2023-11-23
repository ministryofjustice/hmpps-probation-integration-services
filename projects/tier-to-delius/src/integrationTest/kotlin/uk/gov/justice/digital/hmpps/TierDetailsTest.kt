package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.CaseEntityGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisposalTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.OgrsAssessmentGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
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
            .andExpect(MockMvcResultMatchers.jsonPath("$.ogrsscore").value(OgrsAssessmentGenerator.DEFAULT.score))
            .andExpect(MockMvcResultMatchers.jsonPath("$.previousEnforcementActivity").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.registrations[0].code").value(RegistrationGenerator.DEFAULT.type.code))
            .andExpect(MockMvcResultMatchers.jsonPath("$.registrations[0].description").value(RegistrationGenerator.DEFAULT.type.description))
            .andExpect(MockMvcResultMatchers.jsonPath("$.registrations[0].level").value(RegistrationGenerator.DEFAULT.level?.code))
            .andExpect(MockMvcResultMatchers.jsonPath("$.convictions[0].breached").isBoolean)
            .andExpect(MockMvcResultMatchers.jsonPath("$.convictions[0].sentenceTypeCode").value(DisposalTypeGenerator.DEFAULT.sentenceType))
            .andExpect(MockMvcResultMatchers.jsonPath("$.convictions[0].requirements[0].mainCategoryTypeCode").value("MAIN"))
    }

    @Test
    fun `can retrieve all crns`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/probation-cases")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)

        ).andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsString

        val crns = objectMapper.readValue<List<String>>(res)
        assertThat(crns, containsInAnyOrder("A000001", "F001022"))
    }
}
