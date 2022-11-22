package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.RestTemplate
import uk.gov.justice.digital.hmpps.api.allocationdemand.AllocationDemandRepository
import uk.gov.justice.digital.hmpps.api.allocationdemand.AllocationDemandRequest
import uk.gov.justice.digital.hmpps.api.allocationdemand.AllocationRequest
import uk.gov.justice.digital.hmpps.api.allocationdemand.AllocationResponse
import uk.gov.justice.digital.hmpps.api.allocationdemand.CaseType
import uk.gov.justice.digital.hmpps.api.allocationdemand.Event
import uk.gov.justice.digital.hmpps.api.allocationdemand.InitialAppointment
import uk.gov.justice.digital.hmpps.api.allocationdemand.ManagementStatus
import uk.gov.justice.digital.hmpps.api.allocationdemand.Manager
import uk.gov.justice.digital.hmpps.api.allocationdemand.Name
import uk.gov.justice.digital.hmpps.api.allocationdemand.ProbationStatus
import uk.gov.justice.digital.hmpps.api.allocationdemand.Sentence
import java.time.LocalDate

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

    @ParameterizedTest
    @MethodSource("allocationRequests")
    fun `get allocation demand invalid inputs`(allocationRequest: AllocationRequest) {
        mockMvc.perform(
            post("/allocation-demand/")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${getToken()}")
                .content(objectMapper.writeValueAsString(AllocationDemandRequest(listOf(allocationRequest))))
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `get allocation demand expected results`() {
        val request = AllocationDemandRequest(
            listOf(
                AllocationRequest("T123456", "2"),
                AllocationRequest("T456789", "1"),
            )
        )

        val allocationResponse = listOf(
            AllocationResponse(
                "T123456",
                Name("Fred", null, "Flinstone"),
                Event("2", Manager("ST001", Name("John", null, "Smith"), "T001")),
                Sentence("test", LocalDate.now(), "12 Months"),
                InitialAppointment(LocalDate.now()),
                CaseType.CUSTODY,
                ProbationStatus(ManagementStatus.CURRENTLY_MANAGED, null)
            ),
            AllocationResponse(
                "T456789",
                Name("wilma", null, "Flinstone"),
                Event("1", Manager("ST001", Name("John", null, "Smith"), "T001")),
                Sentence("test", LocalDate.now(), "12 Months"),
                InitialAppointment(LocalDate.now()),
                CaseType.CUSTODY,
                ProbationStatus(ManagementStatus.CURRENTLY_MANAGED, null)
            )
        )

        whenever(allocationDemandRepository.findAllocationDemand(any()))
            .thenReturn(allocationResponse)

        mockMvc.perform(
            post("/allocation-demand/")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${getToken()}")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("\$.cases.length()", `is`(2)))
    }

    private fun getToken(): String {
        val authResponse = RestTemplate().postForObject(
            "http://localhost:${wireMockserver.port()}/auth/oauth/token",
            null,
            JsonNode::class.java
        )!!
        return authResponse["access_token"].asText()
    }

    companion object {
        @JvmStatic
        fun allocationRequests(): List<AllocationRequest> = listOf(
            AllocationRequest("D123123!", "1"),
            AllocationRequest("D123123", "1!"),
        )
    }
}
