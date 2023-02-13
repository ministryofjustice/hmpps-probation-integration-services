package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.AllocationDemandRequest
import uk.gov.justice.digital.hmpps.api.model.AllocationRequest
import uk.gov.justice.digital.hmpps.api.model.AllocationResponse
import uk.gov.justice.digital.hmpps.api.model.CaseType
import uk.gov.justice.digital.hmpps.api.model.Event
import uk.gov.justice.digital.hmpps.api.model.InitialAppointment
import uk.gov.justice.digital.hmpps.api.model.ManagementStatus
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.ProbationStatus
import uk.gov.justice.digital.hmpps.api.model.Sentence
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationDemandRepository
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
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
            post("/allocation-demand")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AllocationDemandRequest(listOf())))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `get allocation demand no results`() {

        mockMvc.perform(
            post("/allocation-demand").withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AllocationDemandRequest(listOf())))
        )
            .andExpect(status().is2xxSuccessful)
    }

    @ParameterizedTest
    @MethodSource("allocationRequests")
    fun `get allocation demand invalid inputs`(allocationRequest: AllocationRequest) {
        mockMvc.perform(
            post("/allocation-demand").withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
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
                ProbationStatus(ManagementStatus.CURRENTLY_MANAGED), Manager("JJ001", Name("Chip", null, "Rockefeller"), "T001", "PO")
            ),
            AllocationResponse(
                "T456789",
                Name("wilma", null, "Flinstone"),
                Event("1", Manager("ST001", Name("John", null, "Smith"), "T001")),
                Sentence("test", LocalDate.now(), "12 Months"),
                InitialAppointment(LocalDate.now()),
                CaseType.CUSTODY,
                ProbationStatus(ManagementStatus.CURRENTLY_MANAGED), Manager("JJ001", Name("Chip", null, "Rockefeller"), "T001", "PO")
            )
        )

        whenever(allocationDemandRepository.findAllocationDemand(listOf(Pair("T123456", "2"), Pair("T456789", "1"))))
            .thenReturn(allocationResponse)

        mockMvc.perform(
            post("/allocation-demand").withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("\$.cases.length()", `is`(2)))
    }

    @Test
    fun `unallocated events successful response`() {
        val person = PersonGenerator.DEFAULT
        mockMvc.perform(
            MockMvcRequestBuilders.get("/allocation-demand/${person.crn}/unallocated-events")
                .withOAuth2Token(wireMockserver)
        )
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.crn").value(person.crn))
            .andExpect(jsonPath("$.name.forename").value(person.forename))
            .andExpect(jsonPath("$.name.surname").value(person.surname))
            .andExpect(jsonPath("$.activeEvents[0].eventNumber").value(EventGenerator.DEFAULT.number))
            .andExpect(jsonPath("$.activeEvents[0].teamCode").value(TeamGenerator.DEFAULT.code))
            .andExpect(jsonPath("$.activeEvents[0].providerCode").value(ProviderGenerator.DEFAULT.code))
    }


    companion object {
        @JvmStatic
        fun allocationRequests(): List<AllocationRequest> = listOf(
            AllocationRequest("D123123!", "1"),
            AllocationRequest("D123123", "1!"),
        )
    }
}

