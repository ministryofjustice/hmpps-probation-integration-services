package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_DETAILS
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.utils.Summary

@ExtendWith(MockitoExtension::class)
internal class ScheduleServiceTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var documentRepository: DocumentRepository

    @InjectMocks
    lateinit var service: ScheduleService

    private lateinit var personSummary: Summary

    @BeforeEach
    fun setup() {
        personSummary = Summary(
            id = PERSONAL_DETAILS.id,
            forename = PERSONAL_DETAILS.forename,
            secondName = PERSONAL_DETAILS.secondName,
            surname = PERSONAL_DETAILS.surname, crn = PERSONAL_DETAILS.crn, pnc = PERSONAL_DETAILS.pnc,
            dateOfBirth = PERSONAL_DETAILS.dateOfBirth
        )
    }

    @Test
    fun `calls get upcoming schedule function`() {
        val crn = "X000005"
        val expectedContacts = listOf(ContactGenerator.NEXT_APPT_CONTACT, ContactGenerator.FIRST_APPT_CONTACT)
        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)
        whenever(contactRepository.findUpComingAppointments(any(), any(), any())).thenReturn(expectedContacts)
        val res = service.getPersonUpcomingSchedule(crn)
        assertThat(
            res.personSummary, equalTo(PERSONAL_DETAILS.toSummary())
        )
        assertThat(res.appointments, equalTo(expectedContacts.map { it.toAppointment() }))
    }

    @Test
    fun `calls get appointment function`() {
        val crn = "X000005"
        val expectedContact = ContactGenerator.NEXT_APPT_CONTACT
        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)
        whenever(contactRepository.findByPersonIdAndId(any(), any())).thenReturn(expectedContact)
        whenever(documentRepository.findByPersonIdAndPrimaryKeyId(any(), any())).thenReturn(emptyList())
        val res = service.getPersonAppointment(crn, ContactGenerator.NEXT_APPT_CONTACT.id)
        assertThat(
            res.personSummary, equalTo(PERSONAL_DETAILS.toSummary())
        )
        assertThat(res.appointment, equalTo(expectedContact.toAppointment()))
    }

    @Test
    fun `calls get previous schedule function`() {
        val crn = "X000005"
        val expectedContacts =
            listOf(ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT, ContactGenerator.PREVIOUS_APPT_CONTACT)
        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)
        whenever(contactRepository.findPreviousAppointments(any(), any(), any())).thenReturn(expectedContacts)
        val res = service.getPersonPreviousSchedule(crn)
        assertThat(
            res.personSummary, equalTo(PERSONAL_DETAILS.toSummary())
        )
        assertThat(res.appointments, equalTo(expectedContacts.map { it.toAppointment() }))
    }
}