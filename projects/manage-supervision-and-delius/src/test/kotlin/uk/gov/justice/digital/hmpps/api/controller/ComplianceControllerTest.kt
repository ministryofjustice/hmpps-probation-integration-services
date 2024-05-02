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
import uk.gov.justice.digital.hmpps.api.model.compliance.Breach
import uk.gov.justice.digital.hmpps.api.model.compliance.PersonCompliance
import uk.gov.justice.digital.hmpps.api.model.compliance.SentenceCompliance
import uk.gov.justice.digital.hmpps.api.model.overview.*
import uk.gov.justice.digital.hmpps.service.ComplianceService
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class ComplianceControllerTest {

    @Mock
    lateinit var complianceService: ComplianceService

    @InjectMocks
    lateinit var controller: ComplianceController

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
    fun `calls get get compliance function `() {
        val crn = "X000005"
        val mainOffence = Offence(code = "CD1", description = "Main Offence")
        val order = Order(description = "Order", endDate = LocalDate.now(), length = 1, startDate = LocalDate.now())
        val breach = Breach(LocalDate.now(), "A Breach")
        val rar = Rar(10, 5)
        val activityCount = ActivityCount(1, 0, 1, 0, 1, 1, 1, 2, 4, 1, 0, 1)
        val compliance = Compliance(1, true, 1, 1, 2)
        val sentenceCompliance = SentenceCompliance(
            eventNumber = "3",
            mainOffence = mainOffence,
            order = order,
            activeBreach = breach,
            activity = activityCount,
            rar = rar,
            rarCategory = "Test RAR",
            compliance = compliance
        )
        val previousOrders = PreviousOrders(breaches = 1, count = 1, orders = listOf(order))
        val expectedResponse = PersonCompliance(
            personSummary = personSummary,
            currentSentences = listOf(sentenceCompliance),
            previousOrders = previousOrders
        )
        whenever(complianceService.getPersonCompliance(crn)).thenReturn(expectedResponse)
        val res = controller.getPersonCompliance(crn)
        assertThat(res, equalTo(expectedResponse))
    }
}