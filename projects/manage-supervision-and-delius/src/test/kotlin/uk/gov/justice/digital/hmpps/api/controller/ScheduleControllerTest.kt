package uk.gov.justice.digital.hmpps.api.controller

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.schedule.PersonAppointment
import uk.gov.justice.digital.hmpps.api.model.schedule.Schedule
import uk.gov.justice.digital.hmpps.api.model.schedule.PersonSchedule
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.service.ScheduleService
import uk.gov.justice.digital.hmpps.service.toActivity
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class ScheduleControllerTest {

    @Mock
    lateinit var scheduleService: ScheduleService

    @InjectMocks
    lateinit var controller: ScheduleController

    private lateinit var personSummary: PersonSummary

    @BeforeEach
    fun setup() {
        personSummary = PersonSummary(
            Name(forename = "TestName", middleName = null, surname = "TestSurname"), pnc = "Test PNC",
            crn = "CRN", noms = "NOMS",
            dateOfBirth = LocalDate.now(), offenderId = 1L
        )
    }

    @Test
    fun `calls get get upcoming function `() {
        val crn = "X000005"
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "contact_date", "contact_start_time"))
        val expectedResponse = Schedule(
            personSummary = personSummary,
            PersonSchedule(
                1,
                1,
                2,
                1,
                appointments = listOfNotNull(
                    ContactGenerator.FIRST_APPT_CONTACT.toActivity(),
                    ContactGenerator.NEXT_APPT_CONTACT.toActivity()
                )
            )
        )
        whenever(scheduleService.getPersonUpcomingSchedule(crn, pageable)).thenReturn(expectedResponse)
        val res = controller.getUpcomingSchedule(
            crn,
            page = 0,
            size = 10,
            sortBy = "date",
            ascending = false
        )
        assertThat(res, equalTo(expectedResponse))
    }

    @Test
    fun `calls get get previous function `() {
        val crn = "X000005"
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "contact_date", "contact_start_time"))
        val expectedResponse = Schedule(
            personSummary = personSummary,
            PersonSchedule(
                1,
                1,
                2,
                1,
                appointments = listOfNotNull(
                    ContactGenerator.PREVIOUS_APPT_CONTACT.toActivity(),
                    ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toActivity()
                )
            )
        )
        whenever(scheduleService.getPersonPreviousSchedule(crn, pageable)).thenReturn(expectedResponse)
        val res = controller.getPreviousSchedule(
            crn,
            page = 0,
            size = 10,
            sortBy = "date",
            ascending = false
        )
        assertThat(res, equalTo(expectedResponse))
    }

    @Test
    fun `calls get get appointment function `() {
        val crn = "X000005"
        val id = ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.id
        val expectedResponse = PersonAppointment(
            personSummary = personSummary,
            appointment = ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toActivity()
        )
        whenever(scheduleService.getPersonAppointment(crn, id)).thenReturn(expectedResponse)
        val res = controller.getContact(crn, id)
        assertThat(res, equalTo(expectedResponse))
    }
}