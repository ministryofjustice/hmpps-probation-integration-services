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
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_DETAILS
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.risk.RiskFlagRepository
import uk.gov.justice.digital.hmpps.utils.Summary

@ExtendWith(MockitoExtension::class)
internal class RiskServiceTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var riskFlagRepository: RiskFlagRepository

    @InjectMocks
    lateinit var service: RiskService

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
    fun `calls get risks function`() {
        val crn = "X000005"
        val expectedRiskFlags = listOf(
            PersonGenerator.REGISTRATION_1,
            PersonGenerator.REGISTRATION_2
        )
        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)
        whenever(riskFlagRepository.findByPersonId(any())).thenReturn(expectedRiskFlags)
        val res = service.getPersonRiskFlags(crn)
        assertThat(
            res.personSummary, equalTo(PERSONAL_DETAILS.toSummary())
        )
        assertThat(res.riskFlags, equalTo(expectedRiskFlags.map { it.toRiskFlag() }))
    }

    @Test
    fun `calls get risk function`() {
        val crn = "X000005"
        val expectedRiskFlag = PersonGenerator.REGISTRATION_2
        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)
        whenever(riskFlagRepository.findByPersonIdAndId(any(), any())).thenReturn(expectedRiskFlag)
        val res = service.getPersonRiskFlag(crn, PersonGenerator.REGISTRATION_2.id)
        assertThat(
            res.personSummary, equalTo(PERSONAL_DETAILS.toSummary())
        )
        assertThat(res.riskFlag, equalTo(expectedRiskFlag.toRiskFlag()))
    }
}