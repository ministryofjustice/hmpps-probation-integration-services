package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.conviction.PssRequirement
import uk.gov.justice.digital.hmpps.api.model.conviction.PssRequirements
import uk.gov.justice.digital.hmpps.api.model.keyValueOf
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class PssRequirementsByCrnAndEventIdIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        mockMvc
            .perform(get("/probation-case/$crn/convictions/1/pssRequirements"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `API call probation record not found`() {
        mockMvc
            .perform(get("/probation-case/A123456/convictions/1/pssRequirements").withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Person with crn of A123456 not found"))
    }

    @Test
    fun `API call sentence not found`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        mockMvc
            .perform(get("/probation-case/$crn/convictions/3/pssRequirements").withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Conviction with convictionId 3 not found"))
    }

    @Test
    fun `API call retuns pss requirements by crn convictionId`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        val event = SentenceGenerator.CURRENTLY_MANAGED

        val pssRequirements = listOf(
            PssRequirement(
                ReferenceDataGenerator.PSS_MAIN_CAT.keyValueOf(),
                ReferenceDataGenerator.PSS_SUB_CAT.keyValueOf(),
                true
            ),
            PssRequirement(
                ReferenceDataGenerator.PSS_MAIN_CAT.keyValueOf(),
                ReferenceDataGenerator.PSS_SUB_CAT.keyValueOf(),
                false
            ),
        )

        val expectedResponse = PssRequirements(pssRequirements)

        val response = mockMvc
            .perform(get("/probation-case/$crn/convictions/${event.id}/pssRequirements").withToken())
            .andExpect(status().is2xxSuccessful)
            .andDo(MockMvcResultHandlers.print())
            .andReturn().response.contentAsJson<PssRequirements>()

        assertEquals(expectedResponse, response)
    }
}