package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.activity.Component
import uk.gov.justice.digital.hmpps.api.model.schedule.NextAppointment
import uk.gov.justice.digital.hmpps.api.model.schedule.PersonAppointment
import uk.gov.justice.digital.hmpps.api.model.schedule.Schedule
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import uk.gov.justice.digital.hmpps.api.model.user.PersonManager
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LONG_NOTE
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.service.toActivity
import uk.gov.justice.digital.hmpps.service.toDocument
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import java.time.LocalDate

class ScheduleIntegrationTest : IntegrationTestBase() {

    @Test
    fun `upcoming schedule is returned`() {
        val person = OVERVIEW
        val res = mockMvc.get("/schedule/${person.crn}/upcoming") { withDeliusUserToken("DeliusUser") }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<Schedule>()

        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.personSchedule.appointments[0].id, equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().id))
        assertThat(
            res.personSchedule.appointments[0].type,
            equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().type)
        )
        assertThat(
            res.personSchedule.appointments[0].location?.officeName,
            equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().location?.officeName)
        )
        assertThat(res.personSchedule.appointments[0].location?.postcode, equalTo("H34 7TH"))

        assertThat(res.personSchedule.appointments[0].deliusManaged, equalTo(true))
    }

    @Test
    fun `previous schedule is returned`() {
        val person = OVERVIEW
        val res = mockMvc.get("/schedule/${person.crn}/previous") { withDeliusUserToken("DeliusUser") }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<Schedule>()
        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(
            res.personSchedule.appointments[3].id,
            equalTo(ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toActivity().id)
        )
        assertThat(
            res.personSchedule.appointments[3].type,
            equalTo(ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toActivity().type)
        )
        assertThat(
            res.personSchedule.appointments[3].location?.officeName,
            equalTo(ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toActivity().location?.officeName)
        )
        assertThat(res.personSchedule.appointments[3].location?.postcode, equalTo("H34 7TH"))
    }

    @ParameterizedTest
    @CsvSource(
        "upcoming,10,1,2",
        "upcoming?size=1,1,2,1",
        "upcoming?sortBy=appointment&ascending=false,10,1,2",
        "previous,10,1,6",
        "previous?size=4,4,2,4"
    )
    fun `schedule pagination`(uri: String, requestSize: Int, totalPages: Int, totalResults: Int) {
        val person = OVERVIEW
        val res = mockMvc.get("/schedule/${person.crn}/$uri") { withDeliusUserToken("DeliusUser") }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<Schedule>()

        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.personSchedule.page, equalTo(0))
        assertThat(res.personSchedule.size, equalTo(requestSize))
        assertThat(res.personSchedule.totalPages, equalTo(totalPages))
        assertThat(res.personSchedule.appointments.size, equalTo(totalResults))
    }

    @Test
    fun `previous schedule not found status returned`() {
        mockMvc.get("/schedule/X123456/previous") { withDeliusUserToken("DeliusUser") }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `Upcomimg schedule not found status returned`() {
        mockMvc.get("/schedule/X123456/upcoming") { withDeliusUserToken("DeliusUser") }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `individual appointment is returned`() {

        val person = OVERVIEW
        val id = ContactGenerator.NEXT_APPT_CONTACT.id
        val res = mockMvc.get("/schedule/${person.crn}/appointment/${id}") { withDeliusUserToken("DeliusUser") }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonAppointment>()
        val expectedDocs =
            listOf(
                ContactGenerator.CONTACT_DOCUMENT_1.toDocument(),
                ContactGenerator.CONTACT_DOCUMENT_2.toDocument(),
                ContactGenerator.CONTACT_DOCUMENT_3.toDocument()
            )
        val expectedAppointment = ContactGenerator.NEXT_APPT_CONTACT.toActivity().copy(documents = expectedDocs)
        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.appointment.id, equalTo(expectedAppointment.id))
        assertThat(res.appointment.type, equalTo(expectedAppointment.type))
        assertThat(res.appointment.location?.officeName, equalTo(expectedAppointment.location?.officeName))
        assertThat(res.appointment.documents.size, equalTo(expectedAppointment.documents.size))
        assertThat(res.appointment.location?.postcode, equalTo("H34 7TH"))
        assertThat(res.appointment.description, equalTo(expectedAppointment.description))
        val lc = LicenceConditionGenerator.LC_WITH_NOTES
        assertThat(
            res.appointment.component,
            equalTo(Component(lc.id, lc.mainCategory.description, Component.Type.LICENCE_CONDITION))
        )
    }

    @Test
    fun `individual appointment with outcome is returned`() {

        val person = OVERVIEW
        val id = ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.id
        val res = mockMvc.get("/schedule/${person.crn}/appointment/${id}") { withDeliusUserToken("DeliusUser") }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonAppointment>()

        val expectedNotes = listOf(
            NoteDetail(0, "Tom Brady", LocalDate.of(2024, 10, 29), "was on holiday", false),
            NoteDetail(1, "Harry Kane", LocalDate.of(2024, 10, 29), LONG_NOTE.substring(0, 1500), true)
        )

        assertThat(res.appointment.description, equalTo("previous appointment"))
        assertThat(res.appointment.outcome, equalTo("Acceptable"))
        assertThat(res.appointment.appointmentNotes, equalTo(expectedNotes))
    }

    @Test
    fun `individual appointment with outcome with single note is returned`() {

        val person = OVERVIEW
        val id = ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.id
        val res = mockMvc.get("/schedule/${person.crn}/appointment/${id}/note/1") { withDeliusUserToken("DeliusUser") }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonAppointment>()

        val expectedNote = NoteDetail(1, "Harry Kane", LocalDate.of(2024, 10, 29), LONG_NOTE)

        assertThat(res.appointment.description, equalTo("previous appointment"))
        assertThat(res.appointment.outcome, equalTo("Acceptable"))
        assertThat(res.appointment.appointmentNote, equalTo(expectedNote))
    }

    @Test
    fun `next appointment is returned`() {
        val person = OVERVIEW
        val id = ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.id
        val username = "peter-parker"
        val res = mockMvc.get("/schedule/${person.crn}/next-appointment?contactId=$id&username=$username") {
            withDeliusUserToken("DeliusUser")
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<NextAppointment>()

        assertThat(res.appointment?.type, equalTo("Initial Appointment on Doorstep (NS)"))
        assertThat(res.usernameIsCom, equalTo(true))
        assertThat(res.personManager, equalTo(PersonManager(Name("Peter", null, "Parker"))))
    }
}
