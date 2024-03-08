package uk.gov.justice.digital.hmpps.api.controller

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.overview.Overview
import uk.gov.justice.digital.hmpps.api.model.overview.PersonalDetails
import uk.gov.justice.digital.hmpps.api.model.overview.PreviousOrders
import uk.gov.justice.digital.hmpps.api.model.overview.Schedule
import uk.gov.justice.digital.hmpps.service.OverviewService
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class OverviewControllerTest {

    @Mock
    lateinit var overviewService: OverviewService

    @InjectMocks
    lateinit var controller: OverviewController

    @Test
    fun `calls overview service`() {
        val crn = "X000004"
        val personalDetails = PersonalDetails(
            name = Name(forename = "Joseph", middleName = "Harry", surname = "Bloggs"),
            personalCircumstances = emptyList(),
            disabilities = emptyList(),
            mobileNumber = "1234",
            preferredGender = "Prefer not to say",
            preferredName = "Joe",
            telephoneNumber = "1234",
            dateOfBirth = LocalDate.now().minusYears(50),
            provisions = emptyList()
        )
        val overview = Overview(
            compliance = null,
            personalDetails = personalDetails,
            activity = null,
            previousOrders = PreviousOrders(0, 1),
            sentences = emptyList(),
            schedule = Schedule(null),
            registrations = emptyList()
        )
        whenever(overviewService.getOverview(crn)).thenReturn(overview)
        val res = controller.getOverview("X000004")
        MatcherAssert.assertThat(res.personalDetails.preferredName, Matchers.equalTo("Joe"))
    }
}