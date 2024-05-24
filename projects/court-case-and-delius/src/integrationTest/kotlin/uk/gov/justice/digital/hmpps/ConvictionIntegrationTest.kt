package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.conviction.Conviction
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ConvictionIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `API call probation record not found`() {
        mockMvc
            .perform(get("/probation-case/A123456/convictions/1").withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Person with crn of A123456 not found"))
    }

    @Test
    fun `API call sentence not found`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        mockMvc
            .perform(get("/probation-case/$crn/convictions/3").withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Conviction with ID 3 for Offender with crn C123456 not found"))
    }

    @Test
    fun `API call retuns sentence and custodial status information by crn convictionId`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        val event = SentenceGenerator.CURRENTLY_MANAGED

        val expectedResponse = Conviction(
            event.id, event.eventNumber,
            active = true,
            inBreach = true,
            convictionDate = event.convictionDate
        )
        val response = mockMvc
            .perform(get("/probation-case/$crn/convictions/${event.id}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<Conviction>()

        assertEquals(expectedResponse, response)
    }
}