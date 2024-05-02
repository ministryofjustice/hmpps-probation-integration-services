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
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivity
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.service.ActivityService
import uk.gov.justice.digital.hmpps.service.toActivity
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class ActivityControllerTest {

    @Mock
    lateinit var activityService: ActivityService

    @InjectMocks
    lateinit var controller: ActivityController

    private lateinit var personSummary: PersonSummary

    @BeforeEach
    fun setup() {
        personSummary = PersonSummary(
            Name(forename = "TestName", middleName = null, surname = "TestSurname"), pnc = "Test PNC",
            crn = "CRN",
            dateOfBirth = LocalDate.now(), offenderId = 1L, gender = "Male"
        )
    }

    @Test
    fun `calls get get activity function `() {
        val crn = "X000005"
        val expectedResponse = PersonActivity(
            personSummary = personSummary,
            activities = listOfNotNull(
                ContactGenerator.NEXT_APPT_CONTACT.toActivity(),
                ContactGenerator.FIRST_NON_APPT_CONTACT.toActivity(),
                ContactGenerator.FIRST_APPT_CONTACT.toActivity(),
                ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toActivity(),
                ContactGenerator.PREVIOUS_APPT_CONTACT.toActivity()
            )
        )
        whenever(activityService.getPersonActivity(crn)).thenReturn(expectedResponse)
        val res = controller.getPersonActivity(crn)
        assertThat(res, equalTo(expectedResponse))
    }
}