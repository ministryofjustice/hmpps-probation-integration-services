package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.CaseType.COMMUNITY
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.LdapUserGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.InitialAppointmentData
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class AllocationCompletedServiceTest {
    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var eventRepository: EventRepository

    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var ldapService: LdapService

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var orderManagerRepository: OrderManagerRepository

    @InjectMocks
    lateinit var allocationCompletedService: AllocationCompletedService

    @Test
    fun `missing crn is thrown`() {
        val exception =
            assertThrows<NotFoundException> {
                allocationCompletedService.getDetails("X", "Y", "Z")
            }
        assertThat(exception.message, equalTo("Person with crn of X not found"))
    }

    @Test
    fun `missing event number is thrown`() {
        whenever(personRepository.findByCrnAndSoftDeletedFalse("X")).thenReturn(PersonGenerator.DEFAULT)

        val exception =
            assertThrows<NotFoundException> {
                allocationCompletedService.getDetails("X", "Y", "Z")
            }
        assertThat(exception.message, equalTo("Event Y not found for crn X"))
    }

    @Test
    fun `missing staff or initial appointment is handled`() {
        val person = PersonGenerator.DEFAULT
        val event = EventGenerator.DEFAULT
        whenever(personRepository.findByCrnAndSoftDeletedFalse(person.crn)).thenReturn(person)
        whenever(personRepository.findCaseType(person.crn)).thenReturn(COMMUNITY)
        whenever(eventRepository.findByPersonCrnAndNumber(person.crn, event.number)).thenReturn(event)

        val response = allocationCompletedService.getDetails(person.crn, event.number, "MISSING")

        assertThat(response.staff, nullValue())
        assertThat(response.initialAppointment, nullValue())
    }

    @Test
    fun `missing email address is handled`() {
        val person = PersonGenerator.DEFAULT
        val event = EventGenerator.DEFAULT
        val staff = StaffGenerator.STAFF_WITH_USER
        whenever(personRepository.findByCrnAndSoftDeletedFalse(person.crn)).thenReturn(person)
        whenever(personRepository.findCaseType(person.crn)).thenReturn(COMMUNITY)
        whenever(eventRepository.findByPersonCrnAndNumber(person.crn, event.number)).thenReturn(event)
        whenever(staffRepository.findStaffWithUserByCode(staff.code)).thenReturn(staff)

        val response = allocationCompletedService.getDetails(person.crn, event.number, staff.code)

        assertThat(response.staff!!.email, nullValue())
    }

    @Test
    fun `details response is mapped and returned`() {
        val person = PersonGenerator.DEFAULT
        val event = EventGenerator.DEFAULT
        val staff = StaffGenerator.STAFF_WITH_USER
        val user = LdapUserGenerator.DEFAULT
        val initialAppointmentDate = LocalDate.now()
        whenever(personRepository.findByCrnAndSoftDeletedFalse(person.crn)).thenReturn(person)
        whenever(personRepository.findCaseType(person.crn)).thenReturn(COMMUNITY)
        whenever(eventRepository.findByPersonCrnAndNumber(person.crn, event.number)).thenReturn(event)
        whenever(staffRepository.findStaffWithUserByCode(staff.code)).thenReturn(staff)
        whenever(ldapService.findEmailsForStaffIn(any())).thenReturn(mapOf(user.username to user.email))
        whenever(contactRepository.getInitialAppointmentData(person.id, event.id)).thenReturn(
            object :
                InitialAppointmentData {
                override val date = initialAppointmentDate
                override val staff = staff
            },
        )

        val response = allocationCompletedService.getDetails(person.crn, event.number, staff.code)

        assertThat(response.type, equalTo(COMMUNITY))
        assertThat(response.staff!!.code, equalTo(staff.code))
        assertThat(response.staff!!.grade, equalTo("PSO"))
        assertThat(response.staff!!.email, equalTo(user.email))
        assertThat(response.event.number, equalTo(event.number))
        assertThat(response.event.manager, nullValue())
        assertThat(response.initialAppointment?.date, equalTo(initialAppointmentDate))
    }
}
