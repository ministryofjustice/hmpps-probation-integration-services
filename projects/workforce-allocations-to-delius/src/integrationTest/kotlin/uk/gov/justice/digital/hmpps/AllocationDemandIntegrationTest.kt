package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationDemandRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AllocationDemandIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var allocationDemandRepository: AllocationDemandRepository

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Test
    fun `get allocation demand unauthorised`() {
        mockMvc.perform(post("/allocation-demand").withJson(AllocationDemandRequest(listOf())))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get allocation demand no results`() {
        mockMvc.perform(post("/allocation-demand").withToken().withJson(AllocationDemandRequest(listOf())))
            .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `providing over 500 crns throws exception`() {
        val requests = MutableList(501) { n -> AllocationRequest("T${String.format("%06d", n)}", n.toString()) }

        mockMvc.perform(post("/allocation-demand").withToken().withJson(AllocationDemandRequest(requests)))
            .andExpect(status().is4xxClientError)
    }

    @ParameterizedTest
    @MethodSource("allocationRequests")
    fun `get allocation demand invalid inputs`(allocationRequest: AllocationRequest) {
        mockMvc.perform(
            post("/allocation-demand").withToken().withJson(AllocationDemandRequest(listOf(allocationRequest)))
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `get allocation demand expected results`() {
        val request = AllocationDemandRequest(
            listOf(
                AllocationRequest("T123456", "2"),
                AllocationRequest("T456789", "1")
            )
        )

        val allocationResponse = listOf(
            AllocationResponse(
                "T123456",
                Name("Fred", null, "Flinstone"),
                Event("2", Manager("ST001", Name("John", null, "Smith"), "T001")),
                Event("2", Manager("ST002", Name("John", null, "Smith"), "T002")),
                Sentence("test", LocalDate.now(), "12 Months"),
                InitialAppointment(LocalDate.now(), StaffMember("N01UATU", Name("Unallocated", null, "Staff"))),
                NamedCourt("Court One"),
                CaseType.CUSTODY,
                ProbationStatus(ManagementStatus.CURRENTLY_MANAGED),
                Manager("JJ001", Name("Chip", null, "Rockefeller"), "T001", "PO"),
                LocalDate.of(2023, 12, 31)
            ),
            AllocationResponse(
                "T456789",
                Name("wilma", null, "Flinstone"),
                Event("1", Manager("ST001", Name("John", null, "Smith"), "T001")),
                Event("2", Manager("ST002", Name("John", null, "Smith"), "T002")),
                Sentence("test", LocalDate.now(), "12 Months"),
                InitialAppointment(LocalDate.now(), StaffMember("N01UATU", Name("Unallocated", null, "Staff"))),
                NamedCourt("Court Two"),
                CaseType.CUSTODY,
                ProbationStatus(ManagementStatus.CURRENTLY_MANAGED),
                Manager("JJ001", Name("Chip", null, "Rockefeller"), "T001", "PO"),
                null
            )
        )

        whenever(allocationDemandRepository.findAllocationDemand(listOf(Pair("T123456", "2"), Pair("T456789", "1"))))
            .thenReturn(allocationResponse)

        mockMvc.perform(post("/allocation-demand").withToken().withJson(request))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.cases.length()", `is`(2)))
            .andExpect(jsonPath("$.cases[0].handoverDate").value("2023-12-31"))
            .andExpect(jsonPath("$.cases[1].handoverDate").doesNotExist())
    }

    @Test
    fun `unallocated events successful response`() {
        val person = PersonGenerator.DEFAULT
        mockMvc.perform(get("/allocation-demand/${person.crn}/unallocated-events").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.crn").value(person.crn))
            .andExpect(jsonPath("$.name.forename").value(person.forename))
            .andExpect(jsonPath("$.name.surname").value(person.surname))
            .andExpect(jsonPath("$.activeEvents[0].eventNumber").value(EventGenerator.DEFAULT.number))
            .andExpect(jsonPath("$.activeEvents[0].teamCode").value(TeamGenerator.DEFAULT.code))
            .andExpect(jsonPath("$.activeEvents[0].providerCode").value(ProviderGenerator.DEFAULT.code))
    }

    @Test
    fun `allocation demand allocation staff endpoint`() {
        contactRepository.save(ContactGenerator.INITIAL_APPOINTMENT_CASE_VIEW)
        val person = PersonGenerator.CASE_VIEW
        val event = EventGenerator.CASE_VIEW
        val staff = StaffGenerator.DEFAULT
        val loggedInStaff = StaffGenerator.STAFF_WITH_USER

        mockMvc.perform(
            get("/allocation-demand/${person.crn}/${event.number}/allocation?staff=${staff.code}&allocatingStaffUsername=${loggedInStaff.user!!.username}")
                .withToken()
        )
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.crn").value(person.crn))
            .andExpect(jsonPath("$.name.forename").value(person.forename))
            .andExpect(jsonPath("$.name.surname").value(person.surname))
            .andExpect(jsonPath("$.court.name").value(CourtGenerator.DEFAULT.name))
            .andExpect(jsonPath("$.staff.name.forename").value(staff.forename))
            .andExpect(jsonPath("$.staff.name.surname").value(staff.surname))
            .andExpect(jsonPath("$.staff.grade").value("PSO"))
            .andExpect(jsonPath("$.staff.code").value(staff.code))
            .andExpect(jsonPath("$.allocatingStaff.name.forename").value(loggedInStaff.forename))
            .andExpect(jsonPath("$.allocatingStaff.name.surname").value(loggedInStaff.surname))
            .andExpect(jsonPath("$.allocatingStaff.grade").value("PSO"))
            .andExpect(jsonPath("$.allocatingStaff.code").value(loggedInStaff.code))
            .andExpect(
                jsonPath("$.initialAppointment.date").value(
                    LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                )
            )
            .andExpect(jsonPath("$.sentence.description").value("Case View Sentence Type"))
            .andExpect(jsonPath("$.sentence.code").value("CV"))
            .andExpect(jsonPath("$.offences[0].mainOffence").value(true))
            .andExpect(jsonPath("$.offences[0].mainCategory").value("Case View Main Offence"))
            .andExpect(jsonPath("$.offences[1].mainOffence").value(false))
            .andExpect(jsonPath("$.offences[1].mainCategory").value("Case View Additional Offence"))
            .andExpect(jsonPath("$.activeRequirements[0].mainCategory").value("Main Category for Case View"))
            .andExpect(jsonPath("$.activeRequirements[0].subCategory").value("Rqmnt Sub Category"))
            .andExpect(jsonPath("$.activeRequirements[0].length").value("12 Months"))
            .andExpect(jsonPath("$.activeRequirements[0].manager.allocated").value("false"))
    }

    companion object {
        @JvmStatic
        fun allocationRequests(): List<AllocationRequest> = listOf(
            AllocationRequest("D123123!", "1"),
            AllocationRequest("D123123", "1!")
        )
    }
}
