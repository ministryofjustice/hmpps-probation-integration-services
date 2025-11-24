package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.EnforcementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest
class AppointmentsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository

    @Autowired
    lateinit var contactAlertRepository: ContactAlertRepository

    @Autowired
    lateinit var enforcementRepository: EnforcementRepository

    @Test
    fun `can retrieve appointment details`() {
        val response = mockMvc
            .perform(get("/projects/N01DEFAULT/appointments/${UPWGenerator.DEFAULT_UPW_APPOINTMENT.id}?username=DefaultUser").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<AppointmentResponse>()

        assertThat(response.version).isEqualTo(
            UUID(
                UPWGenerator.DEFAULT_UPW_APPOINTMENT.rowVersion,
                UPWGenerator.DEFAULT_CONTACT.rowVersion
            )
        )
        assertThat(response.project.name).isEqualTo("Default UPW Project")
        assertThat(response.case.crn).isEqualTo(PersonGenerator.DEFAULT_PERSON.crn)
        assertThat(response.penaltyHours).isEqualTo("01:00")
        assertThat(response.enforcementAction!!.respondBy).isEqualTo(response.date.plusDays(ReferenceDataGenerator.ROM_ENFORCEMENT_ACTION.responseByPeriod))
        assertThat(response.behaviour).isEqualTo(Behaviour.EXCELLENT)
        assertThat(response.workQuality).isEqualTo(WorkQuality.EXCELLENT)
    }

    @Test
    fun `can retrieve single session details`() {
        val response = mockMvc
            .perform(
                get(
                    "/projects/N01DEFAULT/appointments?date=${
                        LocalDate.now().plusDays(1)
                    }&username=DefaultUser"
                ).withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<SessionResponse>()

        assertThat(response.project.name).isEqualTo("Default UPW Project")
        assertThat(response.appointmentSummaries.size).isEqualTo(2)
        assertThat(response.appointmentSummaries[0].case.crn).isEqualTo("Z000001")
        assertThat(response.appointmentSummaries[0].requirementProgress.requiredMinutes).isEqualTo(120 * 60)
    }

    @Test
    fun `can update appointment outcome`() {
        mockMvc.put("/projects/N01DEFAULT/appointments/${UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id,
                version = UUID(5, 5),
                outcome = Code("A"),
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(11, 0),
                endTime = LocalTime.of(15, 0),
                notes = "new notes",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 5,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = false,
            )
        }
            .andExpect { status().is2xxSuccessful }

        val appointment =
            unpaidWorkAppointmentRepository.getUpwAppointmentById(UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT.id)

        assertThat(appointment).isNotNull
        assertThat(appointment!!.startTime).isEqualTo(LocalTime.of(11, 0))
        assertThat(appointment.endTime).isEqualTo(LocalTime.of(15, 0))
    }

    @Test
    fun `contact alert created when alertActive is true`() {
        mockMvc.put("/projects/N01DEFAULT/appointments/${UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id,
                version = UUID(5, 5),
                outcome = Code("A"),
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                notes = "contact alert",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 5,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = true,
            )
        }
            .andExpect { status().is2xxSuccessful }

        val alert = contactAlertRepository.findAll()
            .firstOrNull { it.contactId == UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT.contact.id }
        assertThat(alert).isNotNull
    }

    @Test
    fun `enforcement created when complied is false`() {
        mockMvc.put("/projects/N01DEFAULT/appointments/${UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id,
                version = UUID(5, 5),
                outcome = Code("F"),
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                notes = "enforcement",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 5,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = true,
            )
        }
            .andExpect { status().is2xxSuccessful }

        val enforcement = enforcementRepository.findAll()
            .firstOrNull { it.contact.id == UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.contact.id }
        assertThat(enforcement).isNotNull
    }

    @Test
    fun `returns 2xx when limited access check passes but with current restriction flag true`() {
        val appointmentId = UPWGenerator.LAO_RESTRICTED_UPW_APPOINTMENT.id
        val response = mockMvc
            .perform(get("/projects/N01DEFAULT/appointments/$appointmentId?username=LimitedAccess").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<AppointmentResponse>()
        assertThat(response.case.currentRestriction).isEqualTo(true)
        assertThat(response.case.restrictionMessage).isNotNull()
    }

    @Test
    fun `returns 2xx when limited access check fails but with current restriction flag true`() {
        val appointmentId = UPWGenerator.LAO_RESTRICTED_UPW_APPOINTMENT.id
        val response = mockMvc
            .perform(get("/projects/N01DEFAULT/appointments/$appointmentId?username=FullAccess").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<AppointmentResponse>()
        assertThat(response.case.currentRestriction).isEqualTo(false)
        assertThat(response.case.restrictionMessage.isNullOrEmpty())
    }

    @Test
    fun `returns 2xx when limited access check fails but with current exclusion flag true`() {
        val appointmentId = UPWGenerator.LAO_EXCLUDED_UPW_APPOINTMENT.id

        val response = mockMvc
            .perform(get("/projects/N01DEFAULT/appointments/$appointmentId?username=LimitedAccess").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<AppointmentResponse>()

        assertThat(response.case.currentExclusion).isEqualTo(true)
        assertThat(response.case.exclusionMessage).isNotNull
    }

    @Test
    fun `404 if appointment id is invalid`() {
        mockMvc.put("/projects/N01DEFAULT/appointments/987654/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = 987654,
                version = UUID(1, 1),
                outcome = null,
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                notes = "doesn't exist",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 5,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.EXCELLENT,
                sensitive = false,
                alertActive = true,
            )
        }
            .andExpect { status().is4xxClientError }
    }
}