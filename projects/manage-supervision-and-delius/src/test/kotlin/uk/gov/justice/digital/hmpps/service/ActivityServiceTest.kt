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
import uk.gov.justice.digital.hmpps.utils.Summary

@ExtendWith(MockitoExtension::class)
internal class ActivityServiceTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @InjectMocks
    lateinit var service: ActivityService

    private lateinit var personSummary: Summary

    @BeforeEach
    fun setup() {
        personSummary = Summary(
            id = PERSONAL_DETAILS.id,
            forename = PERSONAL_DETAILS.forename,
            secondName = PERSONAL_DETAILS.secondName,
            surname = PERSONAL_DETAILS.surname, crn = PERSONAL_DETAILS.crn, pnc = PERSONAL_DETAILS.pnc,
            dateOfBirth = PERSONAL_DETAILS.dateOfBirth,
            gender = PERSONAL_DETAILS.gender.description
        )
    }

    @Test
    fun `calls get person activity function`() {
        val crn = "X000005"
        val expectedContacts = listOf(
            ContactGenerator.NEXT_APPT_CONTACT,
            ContactGenerator.FIRST_NON_APPT_CONTACT,
            ContactGenerator.FIRST_APPT_CONTACT,
            ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT,
            ContactGenerator.PREVIOUS_APPT_CONTACT
        )
        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)
        whenever(contactRepository.findByPersonId(any())).thenReturn(expectedContacts)
        val res = service.getPersonActivity(crn)
        assertThat(
            res.personSummary, equalTo(PERSONAL_DETAILS.toSummary())
        )
        assertThat(res.activities, equalTo(expectedContacts.map { it.toActivity() }))
        assertThat(res.activities[1].isAppointment, equalTo(false))
    }
}