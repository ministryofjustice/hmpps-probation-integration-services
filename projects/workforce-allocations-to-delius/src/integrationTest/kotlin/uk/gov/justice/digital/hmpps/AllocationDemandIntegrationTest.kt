package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.RestTemplate
import uk.gov.justice.digital.hmpps.api.allocationdemand.AllocationDemandRepository
import uk.gov.justice.digital.hmpps.api.allocationdemand.AllocationDemandRequest
import uk.gov.justice.digital.hmpps.api.allocationdemand.AllocationRequest

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AllocationDemandIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @MockBean
    lateinit var allocationDemandRepository: AllocationDemandRepository

    @Test
    fun `get allocation demand unauthorised`() {
        mockMvc.perform(
            post("/allocation-demand/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AllocationDemandRequest(listOf())))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get allocation demand no results`() {
        val crn = "D123123"
        val eventNumber = "1"
        whenever(allocationDemandRepository.findAllocationDemand(listOf(Pair(crn, eventNumber))))
            .thenReturn(listOf())

        mockMvc.perform(
            post("/allocation-demand/")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${getToken()}")
                .content(objectMapper.writeValueAsString(AllocationDemandRequest(listOf())))
        )
            .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `get allocation demand invalid CRN`() {
        val crn = "D123123!"
        val eventNumber = "1"
        whenever(allocationDemandRepository.findAllocationDemand(listOf(Pair(crn, eventNumber))))
            .thenReturn(listOf())

        val allocationRequest = AllocationRequest(crn, eventNumber)
        mockMvc.perform(
            post("/allocation-demand/")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${getToken()}")
                .content(objectMapper.writeValueAsString(AllocationDemandRequest(listOf(allocationRequest))))
        )
            .andExpect(status().is4xxClientError)
    }

    private fun getToken(): String {
        val authResponse = RestTemplate().postForObject(
            "http://localhost:${wireMockserver.port()}/auth/oauth/token",
            null,
            JsonNode::class.java
        )!!
        return authResponse["access_token"].asText()
    }
}
