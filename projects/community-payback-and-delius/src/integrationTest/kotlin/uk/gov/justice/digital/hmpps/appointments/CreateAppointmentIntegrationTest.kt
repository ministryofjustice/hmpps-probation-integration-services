package uk.gov.justice.digital.hmpps.appointments

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.entity.ContactAlertRepository
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator.UPW_PROJECT_3
import uk.gov.justice.digital.hmpps.entity.sentence.EventRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import uk.gov.justice.digital.hmpps.test.TestData
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
class CreateAppointmentIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
    private val contactAlertRepository: ContactAlertRepository,
    private val eventRepository: EventRepository,
) {
    companion object {
        val PROJECT = UPW_PROJECT_3.code
    }

    @Test
    fun `attempt to create appointment with invalid project code`() {
        mockMvc
            .post("/projects/DOESNOTEXIST/appointments") {
                withToken()
                json = CreateAppointmentsRequest(listOf(TestData.createAppointment()))
            }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("UpwProject with code of DOESNOTEXIST not found")
            }
    }

    @Test
    fun `attempt to create appointment with invalid CRN`() {
        mockMvc
            .post("/projects/$PROJECT/appointments") {
                withToken()
                json = CreateAppointmentsRequest(listOf(TestData.createAppointment().copy(crn = "INVALID")))
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Validation failure")
                assertThat(it.fields!![0].field).isEqualTo("appointments[0].crn")
            }
    }

    @Test
    fun `attempt to create appointment with invalid event number`() {
        mockMvc
            .post("/projects/$PROJECT/appointments") {
                withToken()
                json = CreateAppointmentsRequest(listOf(TestData.createAppointment().copy(eventNumber = -1)))
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Validation failure")
                assertThat(it.fields!![0].field).isEqualTo("appointments[0].eventNumber")
            }
    }

    @Test
    fun `attempt to create appointment with incorrect event number`() {
        mockMvc
            .post("/projects/$PROJECT/appointments") {
                withToken()
                json = CreateAppointmentsRequest(listOf(TestData.createAppointment().copy(eventNumber = 99)))
            }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Event 99 not found for Z000001")
            }
    }

    @Test
    fun `attempt to create appointment with invalid staff code`() {
        mockMvc
            .post("/projects/$PROJECT/appointments") {
                withToken()
                json = CreateAppointmentsRequest(
                    listOf(
                        TestData.createAppointment(),
                        TestData.createAppointment().copy(supervisor = Code("INVALID1")),
                        TestData.createAppointment().copy(supervisor = Code("INVALID2")),
                    )
                )
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Invalid Staff: [INVALID1, INVALID2]")
            }
    }

    @Test
    fun `attempt to create appointment with invalid location code`() {
        mockMvc
            .post("/projects/$PROJECT/appointments") {
                withToken()
                json = CreateAppointmentsRequest(
                    listOf(
                        TestData.createAppointment()
                            .copy(pickUp = CreateAppointmentPickUpData(LocalTime.of(8, 0), Code("INVALID")))
                    )
                )
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Invalid OfficeLocation: [INVALID]")
            }
    }

    @Test
    fun `attempt to create appointment in the past without an outcome`() {
        val request = TestData.createAppointment().copy(date = LocalDate.now().minusDays(1))
        mockMvc
            .post("/projects/$PROJECT/appointments") {
                withToken()
                json = CreateAppointmentsRequest(listOf(request))
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Outcome must be provided when creating an appointment in the past")
            }
    }

    @Test
    fun `attempt to create appointment on an unavailable project day`() {
        val project = UPWGenerator.UPW_PROJECT_2 // project with availability on Mondays

        mockMvc
            .post("/projects/${project.code}/appointments") {
                withToken()
                json = CreateAppointmentsRequest(
                    listOf(
                        TestData.createAppointment().copy(date = LocalDate.of(2026, 1, 1)) // Thursday
                    )
                )
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Project is not available on the following days: [THURSDAY]")
            }
    }

    @Test
    fun `attempt to create appointment after project completion date`() {
        val project = UPWGenerator.COMPLETED_UPW_PROJECT

        mockMvc
            .post("/projects/${project.code}/appointments") {
                withToken()
                json = CreateAppointmentsRequest(listOf(TestData.createAppointment()))
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).contains("Appointment cannot be scheduled after the project completion date")
            }
    }

    @Test
    fun `create future appointments without an outcome`() {
        val request = CreateAppointmentsRequest(
            appointments = List(10) { TestData.createAppointment().copy(reference = UUID(0, it.toLong())) }
        )

        val created = mockMvc
            .post("/projects/$PROJECT/appointments") {
                withToken()
                json = request
            }
            .andExpect { status { isOk() } }
            .andExpect { content { jsonPath("size()") { value(10) } } }
            .andReturn().response.contentAsJson<List<CreatedAppointment>>().map { it.id }

        unpaidWorkAppointmentRepository.findById(created.first()).get().also {
            assertThat(it.date).isEqualTo(LocalDate.now().plusDays(1))
            assertThat(it.startTime).isEqualTo(LocalTime.of(12, 0))
            assertThat(it.endTime).isEqualTo(LocalTime.of(18, 0))
            assertThat(it.minutesOffered).isEqualTo(360)
            assertThat(it.project.code).isEqualTo(PROJECT)
            assertThat(it.allocation?.id).isEqualTo(UPWGenerator.DEFAULT_UPW_ALLOCATION.id)
            assertThat(it.notes).isEqualTo("testing")

            assertThat(it.contact.externalReference).isEqualTo("urn:uk:gov:hmpps:community-payback:appointment:00000000-0000-0000-0000-000000000000")
            assertThat(it.contact.date).isEqualTo(LocalDate.now().plusDays(1))
            assertThat(it.contact.startTime).isEqualTo(LocalTime.of(12, 0))
            assertThat(it.contact.endTime).isEqualTo(LocalTime.of(18, 0))
            assertThat(it.contact.staff.code).isEqualTo(StaffGenerator.DEFAULT_STAFF.code)
            assertThat(it.contact.team.code).isEqualTo(UPW_PROJECT_3.team.code)
            assertThat(it.contact.notes).isEqualTo("testing")
        }
    }

    @Test
    fun `creating an appointment in the past with an outcome is allowed`() {
        val request = TestData.createAppointmentWithOutcome().copy(date = LocalDate.now().minusDays(1))

        val created = mockMvc
            .post("/projects/$PROJECT/appointments") {
                withToken()
                json = CreateAppointmentsRequest(listOf(request))
            }
            .andExpect { status { isOk() } }
            .andExpect { content { jsonPath("size()") { value(1) } } }
            .andReturn().response.contentAsJson<List<CreatedAppointment>>().first()

        assertThat(created.reference).isEqualTo(request.reference)

        unpaidWorkAppointmentRepository.findById(created.id).get().also {
            assertThat(it.date).isEqualTo(LocalDate.now().minusDays(1))
            assertThat(it.outcomeId).isEqualTo(ReferenceDataGenerator.ATTENDED_COMPLIED_CONTACT_OUTCOME.id)
            assertThat(it.workQuality?.code).isEqualTo(WorkQuality.SATISFACTORY.code)
            assertThat(it.behaviour?.code).isEqualTo(Behaviour.EXCELLENT.code)
            assertThat(it.hiVisWorn).isTrue
            assertThat(it.workedIntensively).isFalse
            assertThat(it.attended).isTrue
            assertThat(it.complied).isTrue

            assertThat(it.contact.externalReference).isEqualTo("urn:uk:gov:hmpps:community-payback:appointment:${request.reference}")
            assertThat(it.contact.date).isEqualTo(LocalDate.now().minusDays(1))
            assertThat(it.contact.outcome?.code).isEqualTo(ReferenceDataGenerator.ATTENDED_COMPLIED_CONTACT_OUTCOME.code)
            assertThat(it.contact.attended).isTrue
            assertThat(it.contact.complied).isTrue
        }
    }

    @Test
    fun `creating an appointment with the alert flag creates an alert contact`() {
        val request = TestData.createAppointment().copy(alertActive = true)

        val created = mockMvc
            .post("/projects/$PROJECT/appointments") {
                withToken()
                json = CreateAppointmentsRequest(listOf(request))
            }
            .andExpect { status { isOk() } }
            .andExpect { content { jsonPath("size()") { value(1) } } }
            .andReturn().response.contentAsJson<List<CreatedAppointment>>().first()

        assertThat(created.reference).isEqualTo(request.reference)

        val upwAppointment = unpaidWorkAppointmentRepository.findById(created.id).get()
        assertThat(upwAppointment.contact.alertActive).isTrue
        val alert = contactAlertRepository.findByContactId(upwAppointment.contact.id)
        assertThat(alert).isNotNull
        assertThat(alert!!.contactTypeId).isEqualTo(ReferenceDataGenerator.UPW_APPOINTMENT_TYPE.id)
        assertThat(alert.personId).isEqualTo(PersonGenerator.DEFAULT_PERSON.id)
        assertThat(alert.personManagerId).isEqualTo(PersonGenerator.DEFAULT_PERSON_MANAGER.id)
        assertThat(alert.staffId).isEqualTo(StaffGenerator.DEFAULT_STAFF.id)
        assertThat(alert.teamId).isEqualTo(TeamGenerator.DEFAULT_UPW_TEAM.id)
    }

    @Test
    fun `creating an appointment with a non-complied outcome increments the failure-to-comply count`() {
        eventRepository.getByPersonAndEventNumber(PersonGenerator.DEFAULT_PERSON.id, UPWGenerator.EVENT_3.number).also {
            assertThat(it.ftcCount).isEqualTo(0)
        }
        val request = TestData.createAppointmentWithOutcome().copy(
            outcome = Code(ReferenceDataGenerator.FAILED_TO_ATTEND_CONTACT_OUTCOME.code)
        )

        mockMvc
            .post("/projects/$PROJECT/appointments") {
                withToken()
                json = CreateAppointmentsRequest(listOf(request))
            }
            .andExpect { status { isOk() } }
            .andExpect { content { jsonPath("size()") { value(1) } } }
            .andReturn().response.contentAsJson<List<CreatedAppointment>>()

        eventRepository.getByPersonAndEventNumber(PersonGenerator.DEFAULT_PERSON.id, UPWGenerator.EVENT_3.number).also {
            assertThat(it.ftcCount).isEqualTo(1)
        }
    }

    @Test
    fun `creating an appointment with out a supervisor defaults to unallocated`() {
        val request = TestData.createAppointmentWithOutcome().copy(
            supervisor = null
        )

        val created = mockMvc
            .post("/projects/$PROJECT/appointments") {
                withToken()
                json = CreateAppointmentsRequest(listOf(request))
            }
            .andExpect { status { isOk() } }
            .andExpect { content { jsonPath("size()") { value(1) } } }
            .andReturn().response.contentAsJson<List<CreatedAppointment>>().first()

        assertThat(created.reference).isEqualTo(request.reference)

        unpaidWorkAppointmentRepository.findById(created.id).get().also {
            assertThat(it.contact.staff.code).isEqualTo(StaffGenerator.UNALLOCATED_STAFF.code)
        }
    }
}