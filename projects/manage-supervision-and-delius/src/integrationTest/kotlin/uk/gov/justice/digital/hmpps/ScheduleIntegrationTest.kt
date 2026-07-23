package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.activity.Component
import uk.gov.justice.digital.hmpps.api.model.schedule.LinkedContact
import uk.gov.justice.digital.hmpps.api.model.schedule.NextAppointment
import uk.gov.justice.digital.hmpps.api.model.schedule.PersonAppointment
import uk.gov.justice.digital.hmpps.api.model.schedule.Schedule
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import uk.gov.justice.digital.hmpps.api.model.user.PersonManager
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LONG_NOTE
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
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
        assertThat(res.appointment.alert, equalTo(false))
        val lc = LicenceConditionGenerator.LC_WITH_NOTES
        assertThat(
            res.appointment.component,
            equalTo(Component(lc.id, lc.mainCategory.description, Component.Type.LICENCE_CONDITION))
        )
    }

    @Test
    fun `individual appointment returns alert field as true`() {

        val contact = ContactGenerator.APPT_CONTACT_WITH_ALERT
        val res =
            mockMvc.get("/schedule/${contact.person.crn}/appointment/${contact.id}") { withDeliusUserToken("DeliusUser") }
                .andExpect { status { isOk() } }
                .andReturn().response.contentAsJson<PersonAppointment>()
        assertThat(res.appointment.alert, equalTo(true))
    }

    @Test
    fun `individual appointment with outcome is returned`() {

        val person = OVERVIEW
        val id = ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.id
        val res = mockMvc.get("/schedule/${person.crn}/appointment/${id}") { withDeliusUserToken("DeliusUser") }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonAppointment>()

        assertThat(res.appointment.description, equalTo("previous appointment"))
        assertThat(res.appointment.outcome, equalTo("Acceptable"))
        assertThat(
            res.appointment.appointmentNotes != null && res.appointment.appointmentNotes!!.isNotEmpty(),
            equalTo(true)
        )
        assertThat(res.appointment.eventId, equalTo(PersonGenerator.EVENT_1.id))
        assertThat(res.appointment.eventNumber, equalTo(PersonGenerator.EVENT_1.eventNumber))
    }

    @Test
    fun `individual appointment with outcome with single note is returned`() {

        val person = OVERVIEW
        val id = ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.id
        val res = mockMvc.get("/schedule/${person.crn}/appointment/${id}/note/1") { withDeliusUserToken("DeliusUser") }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonAppointment>()

        assertThat(res.appointment.description, equalTo("previous appointment"))
        assertThat(res.appointment.outcome, equalTo("Acceptable"))
        if (res.appointment.appointmentNote != null) {
            val containsExpected = (res.appointment.appointmentNote?.note?.contains("Licence Condition") == true) ||
                (res.appointment.appointmentNote?.note?.contains("on holiday") == true)
            assertThat(containsExpected, equalTo(true))
        }
    }

    @Test
    fun `appointment not found when contact id does not exist`() {
        val person = OVERVIEW
        mockMvc.get("/schedule/${person.crn}/appointment/999999") { withDeliusUserToken("DeliusUser") }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `appointment not found when crn does not exist`() {
        mockMvc.get("/schedule/X999999/appointment/${ContactGenerator.NEXT_APPT_CONTACT.id}") {
            withDeliusUserToken("DeliusUser")
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `appointment documents contain correct names and alfresco ids`() {
        val person = OVERVIEW
        val id = ContactGenerator.NEXT_APPT_CONTACT.id
        val res = mockMvc.get("/schedule/${person.crn}/appointment/$id") { withDeliusUserToken("DeliusUser") }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonAppointment>()

        assertThat(res.appointment.documents.size, equalTo(3))
        assertThat(
            res.appointment.documents.map { it.name },
            equalTo(listOf("contact.doc", "contact2.doc", "dic.doc"))
        )
    }

    @Test
    fun `appointment with no documents returns empty document list`() {
        val person = OVERVIEW
        val id = ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.id
        val res = mockMvc.get("/schedule/${person.crn}/appointment/$id") { withDeliusUserToken("DeliusUser") }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonAppointment>()

        assertThat(res.appointment.documents.size, equalTo(0))
    }

    @Test
    fun `contacts linked to a contact are returned`() {
        val person = PersonGenerator.LINKED_CONTACT_PERSON
        val contactId = ContactGenerator.INITIAL_CONTACT.id
        val res = mockMvc.get("/schedule/${person.crn}/appointment/$contactId/linked-contacts") {
            withDeliusUserToken("DeliusUser")
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<List<LinkedContact>>()

        assertThat(res.size, equalTo(2))
        assertThat(res[0].contactId, equalTo(ContactGenerator.LINKED_CONTACT_1.id))
        assertThat(res[0].contactTypeDescription, equalTo(ContactGenerator.APPT_CT_1.description))
        assertThat(res[0].contactDate, equalTo(ContactGenerator.LINKED_CONTACT_1.date))
        assertThat(res[1].contactId, equalTo(ContactGenerator.LINKED_CONTACT_2.id))
        assertThat(res[1].contactTypeDescription, equalTo(ContactGenerator.APPT_CT_2.description))
        assertThat(res[1].contactDate, equalTo(ContactGenerator.LINKED_CONTACT_2.date))
    }

    @Test
    fun `enforcement action is returned for appointment`() {
        val person = PersonGenerator.ENFORCEMENT_APPOINTMENT_PERSON
        val contactId = ContactGenerator.ENFORCEMENT_APPOINTMENT_CONTACT.id
        val res = mockMvc.get("/schedule/${person.crn}/appointment/$contactId") { withDeliusUserToken("DeliusUser") }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonAppointment>()

        val enforcement = ContactGenerator.ENFORCEMENT_APPOINTMENT_ENFORCEMENT
        assertThat(res.enforcementAction?.code, equalTo(enforcement.action?.code))
        assertThat(res.enforcementAction?.description, equalTo(enforcement.action?.description))
        assertThat(res.enforcementAction?.responseByDate, equalTo(enforcement.responseDate?.toLocalDate()))
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
