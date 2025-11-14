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
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Behaviour
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.WorkQuality
import uk.gov.justice.digital.hmpps.model.AppointmentOutcomeRequest
import uk.gov.justice.digital.hmpps.model.AppointmentResponse
import uk.gov.justice.digital.hmpps.model.Code
import uk.gov.justice.digital.hmpps.model.SessionResponse
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
        assertThat(response.enforcementAction!!.respondBy).isEqualTo(response.date.plusDays(ReferenceDataGenerator.DEFAULT_ENFORCEMENT_ACTION.responseByPeriod))
        assertThat(response.behaviour).isEqualTo(Behaviour.EX.value)
        assertThat(response.workQuality).isEqualTo(WorkQuality.EX.value)
    }

    @Test
    fun `can retrieve single session details`() {
        val response = mockMvc
            .perform(
                get(
                    "/projects/N01DEFAULT/appointments?date=${
                        LocalDate.now().plusDays(1)
                    }&startTime=12:00&endTime=14:00&username=DefaultUser"
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
                workQuality = "EXCELLENT",
                behaviour = "UNSATISFACTORY",
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
                workQuality = "EXCELLENT",
                behaviour = "UNSATISFACTORY",
                sensitive = false,
                alertActive = true,
            )
        }
            .andExpect { status().is2xxSuccessful }

        val alert = contactAlertRepository.findAll()
            .firstOrNull { it.contactId == UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT.contact.id }
        assertThat(alert).isNotNull
    }
}