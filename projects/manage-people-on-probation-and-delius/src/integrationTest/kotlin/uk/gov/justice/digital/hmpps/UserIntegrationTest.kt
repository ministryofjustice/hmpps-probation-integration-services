package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.TestData.Contact.Type
import uk.gov.justice.digital.hmpps.data.TestData.Person
import uk.gov.justice.digital.hmpps.data.TestData.Users.USER
import uk.gov.justice.digital.hmpps.data.TestData.Users.USER_WITHOUT_STAFF
import uk.gov.justice.digital.hmpps.model.response.Homepage
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UserIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `unknown user returns not found`() {
        mockMvc.get("/user/does-not-exist/homepage") { withToken() }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("User with username of does-not-exist not found") }
            }
    }

    @Test
    fun `user with no staff gets empty homepage`() {
        mockMvc.get("/user/${USER_WITHOUT_STAFF.username}/homepage") { withToken() }
            .andExpect { status { isOk() } }
            .andExpectJson(Homepage(emptyList(), emptyList(), 0))
    }

    @Test
    fun `get user homepage`() {
        mockMvc.get("/user/${USER.username}/homepage") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("$.upcomingAppointments.length()") { value(2) }
                jsonPath("$.appointmentsRequiringOutcome.length()") { value(5) }
                jsonPath("$.appointmentsRequiringOutcomeCount") { value(6) }

                jsonPath("$.upcomingAppointments[0].crn") { value(Person.PERSON_1.crn) }
                jsonPath("$.upcomingAppointments[0].name.forename") { value(Person.PERSON_1.firstName) }
                jsonPath("$.upcomingAppointments[0].name.middleName") { value("Test Test") }
                jsonPath("$.upcomingAppointments[0].name.surname") { value(Person.PERSON_1.surname) }
                jsonPath("$.upcomingAppointments[0].type") { value(Type.PLANNED_OFFICE_VISIT.description) }
                jsonPath("$.upcomingAppointments[0].startDateTime") { value("2030-01-01T09:00:00Z") }
                jsonPath("$.upcomingAppointments[0].endDateTime") { value("2030-01-01T09:30:00Z") }
                jsonPath("$.upcomingAppointments[0].location") { value("Test Office") }
                jsonPath("$.upcomingAppointments[0].deliusManaged") { value(false) }
                jsonPath("$.upcomingAppointments[1].deliusManaged") { value(true) }
                jsonPath("$.upcomingAppointments[1].startDateTime") { value("2030-01-01T10:00:00Z") }
                jsonPath("$.upcomingAppointments[1].endDateTime") { value("2030-01-01T10:30:00Z") }
            }
    }
}
