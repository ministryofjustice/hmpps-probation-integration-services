package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.CaseEntityGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisposalTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.OgrsAssessmentGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class TierDetailsTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `successful response`() {
        mockMvc.perform(
            get("/tier-details/F001022").withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)

        )
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.gender").value(CaseEntityGenerator.DEFAULT.gender.description))
            .andExpect(jsonPath("$.currentTier").value(CaseEntityGenerator.DEFAULT.tier?.code))
            .andExpect(jsonPath("$.rsrscore").value(CaseEntityGenerator.DEFAULT.dynamicRsrScore))
            .andExpect(jsonPath("$.ogrsscore").value(OgrsAssessmentGenerator.DEFAULT.score))
            .andExpect(jsonPath("$.previousEnforcementActivity").value(true))
            .andExpect(jsonPath("$.registrations[0].code").value(RegistrationGenerator.DEFAULT.type.code))
            .andExpect(jsonPath("$.registrations[0].description").value(RegistrationGenerator.DEFAULT.type.description))
            .andExpect(jsonPath("$.registrations[0].level").value(RegistrationGenerator.DEFAULT.level?.code))
            .andExpect(jsonPath("$.convictions[0].breached").isBoolean)
            .andExpect(jsonPath("$.convictions[0].sentenceTypeCode").value(DisposalTypeGenerator.DEFAULT.sentenceType))
            .andExpect(jsonPath("$.convictions[0].requirements[0].mainCategoryTypeCode").value("MAIN"))
    }

    @Test
    fun `can retrieve person details`() {
        mockMvc.perform(get("/person/A000001").withOAuth2Token(wireMockserver))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.crn", equalTo("A000001")))
            .andExpect(jsonPath("$.name.forenames", equalTo("Test")))
            .andExpect(jsonPath("$.name.surname", equalTo("Person")))
    }

    @Test
    fun `can retrieve all crns`() {
        val res = mockMvc.perform(
            get("/probation-cases")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)

        ).andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsString

        val crns = objectMapper.readValue<List<String>>(res)
        assertThat(crns, containsInAnyOrder("F001022"))
    }
}
