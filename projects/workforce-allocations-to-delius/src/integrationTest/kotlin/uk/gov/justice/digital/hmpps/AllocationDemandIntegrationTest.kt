package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationDemandRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AllocationDemandIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {
    @MockitoBean
    lateinit var allocationDemandRepository: AllocationDemandRepository

    @Test
    fun `get allocation demand unauthorised`() {
        mockMvc.post("/allocation-demand") { json = AllocationDemandRequest(listOf()) }
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `get allocation demand no results`() {
        mockMvc.post("/allocation-demand") {
            withToken()
            json = AllocationDemandRequest(listOf())
        }
            .andExpect { status { is2xxSuccessful() } }
    }

    @Test
    fun `providing over 500 crns throws exception`() {
        val requests = MutableList(501) { n -> AllocationRequest("T${String.format("%06d", n)}", n.toString()) }

        mockMvc.post("/allocation-demand") {
            withToken()
            json = AllocationDemandRequest(requests)
        }
            .andExpect { status { is4xxClientError() } }
    }

    @ParameterizedTest
    @MethodSource("allocationRequests")
    fun `get allocation demand invalid inputs`(allocationRequest: AllocationRequest) {
        mockMvc.post("/allocation-demand") {
            withToken()
            json = AllocationDemandRequest(listOf(allocationRequest))
        }
            .andExpect { status { is4xxClientError() } }
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

        mockMvc.post("/allocation-demand") {
            withToken()
            json = request
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.cases.length()") { value(2) }
                jsonPath("$.cases[0].handoverDate") { value("2023-12-31") }
                jsonPath("$.cases[1].handoverDate") { doesNotExist() }

            }
    }

    @Test
    fun `unallocated events successful response`() {
        val person = PersonGenerator.DEFAULT
        mockMvc.get("/allocation-demand/${person.crn}/unallocated-events") {
            withToken()
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.crn") { value(person.crn) }
                jsonPath("$.name.forename") { value(person.forename) }
                jsonPath("$.name.surname") { value(person.surname) }
                jsonPath("$.activeEvents[0].eventNumber") { value(EventGenerator.DEFAULT.number) }
                jsonPath("$.activeEvents[0].teamCode") { value(TeamGenerator.DEFAULT.code) }
                jsonPath("$.activeEvents[0].providerCode") { value(ProviderGenerator.DEFAULT.code) }
            }
    }

    @Test
    fun `allocation demand allocation staff endpoint`() {

        val person = PersonGenerator.CASE_VIEW
        val event = EventGenerator.CASE_VIEW
        val staff = StaffGenerator.DEFAULT
        val loggedInStaff = StaffGenerator.STAFF_WITH_USER

        mockMvc.get("/allocation-demand/${person.crn}/${event.number}/allocation?staff=${staff.code}&allocatingStaffUsername=${loggedInStaff.user!!.username}") {
            withToken()
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.crn") { value(person.crn) }
                jsonPath("$.name.forename") { value(person.forename) }
                jsonPath("$.name.surname") { value(person.surname) }
                jsonPath("$.court.name") { value(CourtGenerator.DEFAULT.name) }
                jsonPath("$.staff.name.forename") { value(staff.forename) }
                jsonPath("$.staff.name.surname") { value(staff.surname) }
                jsonPath("$.staff.grade") { value("PSO") }
                jsonPath("$.staff.code") { value(staff.code) }
                jsonPath("$.allocatingStaff.name.forename") { value(loggedInStaff.forename) }
                jsonPath("$.allocatingStaff.name.surname") { value(loggedInStaff.surname) }
                jsonPath("$.allocatingStaff.grade") { value("PSO") }
                jsonPath("$.allocatingStaff.code") { value(loggedInStaff.code) }

                //TODO: will be handled in future story
                /*jsonPath("$.initialAppointment.date") {
                    value(
                        LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                    )
                }*/
                jsonPath("$.sentence.description") { value("Case View Sentence Type") }
                jsonPath("$.sentence.code") { value("CV") }
                jsonPath("$.offences[0].mainOffence") { value(true) }
                jsonPath("$.offences[0].mainCategory") { value("Case View Main Offence") }
                jsonPath("$.offences[1].mainOffence") { value(false) }
                jsonPath("$.offences[1].mainCategory") { value("Case View Additional Offence") }
                jsonPath("$.activeRequirements[0].mainCategory") { value("Main Category for Case View") }
                jsonPath("$.activeRequirements[0].subCategory") { value("Rqmnt Sub Category") }
                jsonPath("$.activeRequirements[0].length") { value("12 Months") }
                jsonPath("$.activeRequirements[0].manager.allocated") { value("false") }
            }
    }

    companion object {
        @JvmStatic
        fun allocationRequests(): List<AllocationRequest> = listOf(
            AllocationRequest("D123123!", "1"),
            AllocationRequest("D123123", "1!")
        )
    }
}
