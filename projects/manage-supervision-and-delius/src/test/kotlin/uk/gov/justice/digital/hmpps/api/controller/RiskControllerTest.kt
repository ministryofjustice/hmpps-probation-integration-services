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
import uk.gov.justice.digital.hmpps.api.model.risk.PersonRiskFlag
import uk.gov.justice.digital.hmpps.api.model.risk.PersonRiskFlags
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REGISRATION_1
import uk.gov.justice.digital.hmpps.service.RiskService
import uk.gov.justice.digital.hmpps.service.toRiskFlag
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class RiskControllerTest {

    @Mock
    lateinit var riskService: RiskService

    @InjectMocks
    lateinit var controller: RiskController

    private lateinit var personSummary: PersonSummary

    @BeforeEach
    fun setup() {
        personSummary = PersonSummary(
            Name(forename = "TestName", middleName = null, surname = "TestSurname"), pnc = "Test PNC",
            crn = "CRN",
            dateOfBirth = LocalDate.now(), offenderId = 1L
        )
    }

    @Test
    fun `calls get get risk flags function `() {
        val crn = "X000005"
        val expectedResponse = PersonRiskFlags(
            personSummary = personSummary,
            riskFlags = listOfNotNull(
                PersonGenerator.REGISRATION_1.toRiskFlag(),
                PersonGenerator.REGISRATION_2.toRiskFlag()
            )
        )
        whenever(riskService.getPersonRiskFlags(crn)).thenReturn(expectedResponse)
        val res = controller.getPersonRiskFlags(crn)
        assertThat(res, equalTo(expectedResponse))
    }

    @Test
    fun `calls get get risk flag function `() {
        val crn = "X000005"
        val expectedResponse = PersonRiskFlag(
            personSummary = personSummary,
            riskFlag = PersonGenerator.REGISRATION_1.toRiskFlag()
        )
        whenever(riskService.getPersonRiskFlag(crn, REGISRATION_1.id)).thenReturn(expectedResponse)
        val res = controller.getPersonRiskFlag(crn, REGISRATION_1.id)
        assertThat(res, equalTo(expectedResponse))
    }
}