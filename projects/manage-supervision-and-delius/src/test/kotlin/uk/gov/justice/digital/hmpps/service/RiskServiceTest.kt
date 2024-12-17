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
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.MAPPA_TYPE
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.generateRegistration
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_DETAILS
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.risk.RiskFlagRepository
import uk.gov.justice.digital.hmpps.utils.Summary

@ExtendWith(MockitoExtension::class)
internal class RiskServiceTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var riskFlagRepository: RiskFlagRepository

    @Mock
    lateinit var nsiRepository: NsiRepository

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
        whenever(nsiRepository.findByPersonIdAndTypeCode(any(), any())).thenReturn(emptyList())
        whenever(riskFlagRepository.findActiveMappaRegistrationByOffenderId(any(), any())).thenReturn(Page.empty())

        val res = service.getPersonRiskFlags(crn)
        assertThat(
            res.personSummary, equalTo(PERSONAL_DETAILS.toSummary())
        )
        assertThat(res.riskFlags, equalTo(expectedRiskFlags.map { it.toRiskFlag() }))
    }

    @Test
    fun `returns nominal community level and category for mappa`() {
        val crn = "X000005"
        val expectedRiskFlags = listOf(
            PersonGenerator.REGISTRATION_1,
            PersonGenerator.REGISTRATION_2
        )

        val otherMappaLevel = ReferenceData(IdGenerator.getAndIncrement(), "OT", "Other Desc")
        val otherMappaCategory = ReferenceData(IdGenerator.getAndIncrement(), "OT", "Other Desc")
        val mappaRegistration =
            generateRegistration(MAPPA_TYPE, otherMappaCategory, 1L, "Notes", level = otherMappaLevel)

        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)
        whenever(riskFlagRepository.findByPersonId(any())).thenReturn(expectedRiskFlags)
        whenever(nsiRepository.findByPersonIdAndTypeCode(any(), any())).thenReturn(emptyList())
        whenever(riskFlagRepository.findActiveMappaRegistrationByOffenderId(any(), any())).thenReturn(
            PageImpl(
                listOf(
                    mappaRegistration
                )
            )
        )

        val res = service.getPersonRiskFlags(crn)
        assertThat(res.mappa?.level, equalTo(MappaLevel.NOMINAL.communityValue))
        assertThat(res.mappa?.category, equalTo(MappaLevel.NOMINAL.communityValue))
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