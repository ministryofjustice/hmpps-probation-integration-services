package uk.gov.justice.digital.hmpps.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.oasys.client.OasysClient
import uk.gov.justice.digital.hmpps.integrations.oasys.model.Inputs
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysHealthAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysHealthDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysNeedsAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysNeedsDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysOffenceAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysOffenceDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskAssessmentDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskManagementPlanAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskManagementPlanDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskToTheIndividualAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskToTheIndividualDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRoshAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRoshDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRoshSummary
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRoshSummaryAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysTimelineAssessment
import uk.gov.justice.digital.hmpps.model.Health
import uk.gov.justice.digital.hmpps.model.HealthDetail
import uk.gov.justice.digital.hmpps.model.HealthDetails
import uk.gov.justice.digital.hmpps.model.LinksToHarm
import uk.gov.justice.digital.hmpps.model.LinksToReOffending
import uk.gov.justice.digital.hmpps.model.Needs
import uk.gov.justice.digital.hmpps.model.NeedsDetails
import uk.gov.justice.digital.hmpps.model.Offence
import uk.gov.justice.digital.hmpps.model.OffenceDetails
import uk.gov.justice.digital.hmpps.model.RiskAssessment
import uk.gov.justice.digital.hmpps.model.RiskAssessmentDetails
import uk.gov.justice.digital.hmpps.model.RiskLevel
import uk.gov.justice.digital.hmpps.model.RiskManagementPlan
import uk.gov.justice.digital.hmpps.model.RiskManagementPlanDetails
import uk.gov.justice.digital.hmpps.model.RiskToTheIndividual
import uk.gov.justice.digital.hmpps.model.RiskToTheIndividualDetails
import uk.gov.justice.digital.hmpps.model.Rosh
import uk.gov.justice.digital.hmpps.model.RoshDetails
import uk.gov.justice.digital.hmpps.model.RoshSummary
import uk.gov.justice.digital.hmpps.model.RoshSummaryDetails
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class OasysAssessmentServiceTest {

    @Mock
    lateinit var oasysClient: OasysClient

    @InjectMocks
    lateinit var oasysAssessmentService: OasysAssessmentService

    private val crn = "D123456"
    private val assessmentPk = 12345L
    private val status = "COMPLETE"
    private val assessmentType = "LAYER3"
    private val now = ZonedDateTime.now()
    private lateinit var oasysAssessmentTimeline: OasysAssessmentTimeline

    @BeforeEach
    fun setup() {
        oasysAssessmentTimeline = OasysAssessmentTimeline(
            crn = crn,
            source = "OASys",
            limitedAccessOffender = false,
            inputs = Inputs(crn, laoPrivilege = "false"),
            timeline = listOf(
                OasysTimelineAssessment(assessmentPk, assessmentType, now, status, now)
            )
        )

        whenever(oasysClient.getAssessmentTimeline(crn)).thenReturn(oasysAssessmentTimeline)
    }

    @Test
    fun `should not attempt to retrieve offence details where CRN does not exist`() {
        // Given
        val crn = "D123456"
        whenever(oasysClient.getAssessmentTimeline(crn)).thenThrow(
            HttpClientErrorException(
                HttpStatus.NOT_FOUND,
                "CRN Not found for $crn"
            )
        )

        // When
        val exception = assertThrows<NotFoundException> {
            oasysAssessmentService.getOffenceDetails(crn)
        }

        // Then
        assertThat(exception.message).isEqualTo("404 CRN Not found for $crn")
        verify(never()) { oasysClient.getOffenceDetails(any(), any(), any()) }
    }

    @Test
    fun `should return OffenceDetails for valid CRN`() {
        // Given
        val oasysOffenceDetails = OasysOffenceDetails(
            limitedAccessOffender = false,
            listOf(
                OasysOffenceAssessment(
                    assessmentPk = assessmentPk,
                    assessmentType = assessmentType,
                    initiationDate = now,
                    assessmentStatus = status,
                    offenceAnalysis = "analysis",
                    dateCompleted = now,
                    lastUpdatedDate = now
                )
            )
        )
        whenever(oasysClient.getOffenceDetails(crn, assessmentPk, status)).thenReturn(oasysOffenceDetails)

        // When
        val offenceDetails = oasysAssessmentService.getOffenceDetails(crn)

        // Then
        val expectedOffenceDetails = OffenceDetails(
            assessmentPk,
            assessmentType,
            now,
            assessorSignedDate = null,
            initiationDate = now,
            status,
            null,
            null,
            false,
            lastUpdatedDate = now,
            Offence(
                offenceAnalysis = "analysis"
            )
        )
        assertThat(offenceDetails).isEqualTo(expectedOffenceDetails)
    }

    @Test
    fun `should not attempt to retrieve needs details where CRN does not exist`() {
        // Given
        val crn = "D123456"
        whenever(oasysClient.getAssessmentTimeline(crn)).thenThrow(
            HttpClientErrorException(
                HttpStatus.NOT_FOUND,
                "CRN Not found for $crn"
            )
        )

        // When
        val exception = assertThrows<NotFoundException> {
            oasysAssessmentService.getNeedsDetails(crn)
        }

        // Then
        assertThat(exception.message).isEqualTo("404 CRN Not found for $crn")
        verify(never()) { oasysClient.getNeedsDetails(any(), any(), any()) }
    }

    @Test
    fun `should return NeedsDetails without LinksForHarm for valid CRN when links for harm data not present`() {
        // Given
        val oasysNeedsDetails = OasysNeedsDetails(
            limitedAccessOffender = false,
            listOf(
                OasysNeedsAssessment(
                    assessmentPk = assessmentPk,
                    assessmentType = assessmentType,
                    initiationDate = now,
                    assessmentStatus = status,
                    dateCompleted = now,
                    offenceAnalysisDetails = "analysis",
                    drugLinkedToReoffending = "No",
                    attLinkedToReoffending = "No",
                    relLinkedToReoffending = "Yes",
                    lifestyleIssuesDetails = "Has lifestyle issues",
                    thingIssuesDetails = "Thinking issues",
                    lastUpdatedDate = now
                )
            )
        )
        whenever(oasysClient.getNeedsDetails(crn, assessmentPk, status)).thenReturn(oasysNeedsDetails)

        // When
        val needsDetails = oasysAssessmentService.getNeedsDetails(crn)

        // Then
        val expectedNeedsDetails = NeedsDetails(
            assessmentPk,
            assessmentType,
            now,
            null,
            now,
            status,
            null,
            null,
            false,
            now,
            Needs(
                offenceAnalysisDetails = "analysis",
                lifestyleIssuesDetails = "Has lifestyle issues",
                thinkingBehaviouralIssuesDetails = "Thinking issues"
            ),
            linksToHarm = null,
            LinksToReOffending(
                drugLinkedToReOffending = false,
                attitudeLinkedToReOffending = false,
                relationshipLinkedToReOffending = true
            )
        )
        assertThat(needsDetails).isEqualTo(expectedNeedsDetails)
    }

    @Test
    fun `should return NeedsDetails for valid CRN`() {
        // Given
        val oasysNeedsDetails = OasysNeedsDetails(
            limitedAccessOffender = false,
            listOf(
                OasysNeedsAssessment(
                    assessmentPk = assessmentPk,
                    assessmentType = assessmentType,
                    initiationDate = now,
                    assessmentStatus = status,
                    dateCompleted = now,
                    offenceAnalysisDetails = "analysis",
                    emoLinkedToHarm = "Yes",
                    drugLinkedToReoffending = "No",
                    alcoholLinkedToHarm = "Don't Know",
                    attLinkedToReoffending = "No",
                    financeLinkedToHarm = "Yes",
                    relLinkedToReoffending = "Yes",
                    lifestyleIssuesDetails = "Has lifestyle issues",
                    thingIssuesDetails = "Thinking issues",
                    lastUpdatedDate = now
                )
            )
        )
        whenever(oasysClient.getNeedsDetails(crn, assessmentPk, status)).thenReturn(oasysNeedsDetails)

        // When
        val needsDetails = oasysAssessmentService.getNeedsDetails(crn)

        // Then
        val expectedNeedsDetails = NeedsDetails(
            assessmentPk,
            assessmentType,
            now,
            null,
            now,
            status,
            null,
            null,
            false,
            lastUpdatedDate = now,
            Needs(
                offenceAnalysisDetails = "analysis",
                lifestyleIssuesDetails = "Has lifestyle issues",
                thinkingBehaviouralIssuesDetails = "Thinking issues"
            ),
            LinksToHarm(
                emotionalLinkedToHarm = true,
                alcoholLinkedToHarm = null,
                financeLinkedToHarm = true
            ),
            LinksToReOffending(
                drugLinkedToReOffending = false,
                attitudeLinkedToReOffending = false,
                relationshipLinkedToReOffending = true
            )
        )
        assertThat(needsDetails).isEqualTo(expectedNeedsDetails)
    }

    @Test
    fun `should not attempt to retrieve risk management plan details where CRN does not exist`() {
        // Given
        val crn = "D123456"
        whenever(oasysClient.getAssessmentTimeline(crn)).thenThrow(
            HttpClientErrorException(
                HttpStatus.NOT_FOUND,
                "CRN Not found for $crn"
            )
        )

        // When
        val exception = assertThrows<NotFoundException> {
            oasysAssessmentService.getRiskManagementPlanDetails(crn)
        }

        // Then
        assertThat(exception.message).isEqualTo("404 CRN Not found for $crn")
        verify(never()) { oasysClient.getRiskManagementPlanDetails(any(), any(), any()) }
    }

    @Test
    fun `should return RiskManagementPlanDetails for valid CRN`() {
        // Given
        val oasysRiskManagementPlanDetails = OasysRiskManagementPlanDetails(
            limitedAccessOffender = false,
            listOf(
                OasysRiskManagementPlanAssessment(
                    assessmentPk = assessmentPk,
                    assessmentType = assessmentType,
                    initiationDate = now,
                    assessmentStatus = status,
                    dateCompleted = now,
                    keyInformationAboutCurrentSituation = "key info"
                )
            )
        )
        whenever(oasysClient.getRiskManagementPlanDetails(crn, assessmentPk, status)).thenReturn(
            oasysRiskManagementPlanDetails
        )

        // When
        val riskManagementPlanDetails = oasysAssessmentService.getRiskManagementPlanDetails(crn)

        // Then
        val expectedRiskManagementPlanDetails = RiskManagementPlanDetails(
            assessmentId = assessmentPk,
            assessmentType = assessmentType,
            initiationDate = now,
            dateCompleted = now,
            assessmentStatus = status,
            limitedAccessOffender = false,
            riskManagementPlan = RiskManagementPlan(
                keyInformationAboutCurrentSituation = "key info"
            )
        )
        assertThat(riskManagementPlanDetails).isEqualTo(expectedRiskManagementPlanDetails)
    }

    @Test
    fun `should not attempt to retrieve rosh summary details where CRN does not exist`() {
        // Given
        val crn = "D123456"
        whenever(oasysClient.getAssessmentTimeline(crn)).thenThrow(
            HttpClientErrorException(
                HttpStatus.NOT_FOUND,
                "CRN Not found for $crn"
            )
        )

        // When
        val exception = assertThrows<NotFoundException> {
            oasysAssessmentService.getRoshSummary(crn)
        }

        // Then
        assertThat(exception.message).isEqualTo("404 CRN Not found for $crn")
        verify(never()) { oasysClient.getRoshSummary(any(), any(), any()) }
    }

    @Test
    fun `should return RoshSummary for valid CRN`() {
        // Given
        val oasysRoshSummary = OasysRoshSummary(
            limitedAccessOffender = false,
            listOf(
                OasysRoshSummaryAssessment(
                    assessmentPk = assessmentPk,
                    assessmentType = assessmentType,
                    initiationDate = now,
                    assessmentStatus = status,
                    dateCompleted = now,
                    whoAtRisk = "I am!"
                )
            )
        )
        whenever(oasysClient.getRoshSummary(crn, assessmentPk, status)).thenReturn(oasysRoshSummary)

        // When
        val roshSummary = oasysAssessmentService.getRoshSummary(crn)

        // Then
        val expectedRoshSummary = RoshSummaryDetails(
            assessmentId = assessmentPk,
            assessmentType = assessmentType,
            initiationDate = now,
            dateCompleted = now,
            assessmentStatus = status,
            limitedAccessOffender = false,
            roshSummary = RoshSummary(
                whoIsAtRisk = "I am!"
            )
        )
        assertThat(roshSummary).isEqualTo(expectedRoshSummary)
    }

    @Test
    fun `should not attempt to retrieve risk to the individual details where CRN does not exist`() {
        // Given
        val crn = "D123456"
        whenever(oasysClient.getAssessmentTimeline(crn)).thenThrow(
            HttpClientErrorException(
                HttpStatus.NOT_FOUND,
                "CRN Not found for $crn"
            )
        )

        // When
        val exception = assertThrows<NotFoundException> {
            oasysAssessmentService.getRiskToIndividual(crn)
        }

        // Then
        assertThat(exception.message).isEqualTo("404 CRN Not found for $crn")
        verify(never()) { oasysClient.getRiskToTheIndividual(any(), any(), any()) }
    }

    @Test
    fun `should return RiskToIndividual for valid CRN`() {
        // Given
        val oasysRiskToTheIndividualAssessmentDetails = OasysRiskToTheIndividualDetails(
            limitedAccessOffender = false,
            listOf(
                OasysRiskToTheIndividualAssessment(
                    assessmentPk = assessmentPk,
                    assessmentType = assessmentType,
                    initiationDate = now,
                    assessmentStatus = status,
                    dateCompleted = now,
                    concernsBreachOfTrust = "Yes",
                    concernsRiskOfSuicide = "No",
                    previousVulnerability = "previously vulnerable",
                    currentConcernsBreachOfTrust = "Yes",
                    concernsRiskOfSelfHarm = "No"
                )
            )
        )
        whenever(oasysClient.getRiskToTheIndividual(crn, assessmentPk, status)).thenReturn(
            oasysRiskToTheIndividualAssessmentDetails
        )

        // When
        val riskToTheIndividualDetails = oasysAssessmentService.getRiskToIndividual(crn)

        // Then
        val expectedRiskToTheIndividualDetails = RiskToTheIndividualDetails(
            assessmentId = assessmentPk,
            assessmentType = assessmentType,
            initiationDate = now,
            dateCompleted = now,
            assessmentStatus = status,
            limitedAccessOffender = false,
            riskToTheIndividual = RiskToTheIndividual(
                concernsBreachOfTrust = true,
                concernsRiskOfSuicide = false,
                previousVulnerability = "previously vulnerable",
                currentConcernsBreachOfTrust = true,
                concernsRiskOfSelfHarm = false
            )
        )
        assertThat(riskToTheIndividualDetails).isEqualTo(expectedRiskToTheIndividualDetails)
    }

    @Test
    fun `should not attempt to retrieve risk assessment details where CRN does not exist`() {
        // Given
        val crn = "D123456"
        whenever(oasysClient.getAssessmentTimeline(crn)).thenThrow(
            HttpClientErrorException(
                HttpStatus.NOT_FOUND,
                "CRN Not found for $crn"
            )
        )

        // When
        val exception = assertThrows<NotFoundException> {
            oasysAssessmentService.getRiskAssessment(crn)
        }

        // Then
        assertThat(exception.message).isEqualTo("404 CRN Not found for $crn")
        verify(never()) { oasysClient.getRiskAssessment(any(), any(), any()) }
    }

    @Test
    fun `should return RiskAssessment for valid CRN`() {
        // Given
        val oasysRiskAssessmentDetails = OasysRiskAssessmentDetails(
            limitedAccessOffender = false,
            listOf(
                OasysRiskAssessment(
                    assessmentPk = assessmentPk,
                    assessmentType = assessmentType,
                    initiationDate = now,
                    assessmentStatus = status,
                    dateCompleted = now,
                    currentOffenceDetails = "fight",
                    previousWhereAndWhen = "in the library",
                    previousWhatDone = "Stabbing",
                    currentSources = "witnesses",
                    currentWhyDone = "for money",
                    currentAnyoneElsePresent = "gang of people"
                )
            )
        )
        whenever(oasysClient.getRiskAssessment(crn, assessmentPk, status)).thenReturn(oasysRiskAssessmentDetails)

        // When
        val riskAssessmentDetails = oasysAssessmentService.getRiskAssessment(crn)

        // Then
        val expectedRiskAssessmentDetails = RiskAssessmentDetails(
            assessmentId = assessmentPk,
            assessmentType = assessmentType,
            initiationDate = now,
            dateCompleted = now,
            assessmentStatus = status,
            limitedAccessOffender = false,
            riskAssessment = RiskAssessment(
                currentOffenceDetails = "fight",
                previousWhereAndWhen = "in the library",
                previousWhatDone = "Stabbing",
                currentSources = "witnesses",
                currentWhyDone = "for money",
                currentAnyoneElsePresent = "gang of people"
            )
        )
        assertThat(riskAssessmentDetails).isEqualTo(expectedRiskAssessmentDetails)
    }

    @Test
    fun `should not attempt to retrieve ROSH details where CRN does not exist`() {
        // Given
        val crn = "D123456"
        whenever(oasysClient.getAssessmentTimeline(crn)).thenThrow(
            HttpClientErrorException(
                HttpStatus.NOT_FOUND,
                "CRN Not found for $crn"
            )
        )

        // When
        val exception = assertThrows<NotFoundException> {
            oasysAssessmentService.getRosh(crn)
        }

        // Then
        assertThat(exception.message).isEqualTo("404 CRN Not found for $crn")
        verify(never()) { oasysClient.getRiskOfSeriousHarm(any(), any(), any()) }
    }

    @Test
    fun `should return ROSH for valid CRN`() {
        // Given
        val oasysRoshDetails = OasysRoshAssessment(
            limitedAccessOffender = false,
            listOf(
                OasysRoshDetails(
                    assessmentPk = assessmentPk,
                    assessmentType = assessmentType,
                    initiationDate = now,
                    assessmentStatus = status,
                    dateCompleted = now,
                    riskChildrenCommunity = "Low",
                    riskPrisonersCustody = "Medium",
                    riskStaffCustody = "Medium",
                    riskStaffCommunity = "Medium",
                    riskKnownAdultCustody = null,
                    riskKnownAdultCommunity = "Medium",
                    riskPublicCustody = "High",
                    riskChildrenCustody = "Very High"
                )
            )
        )
        whenever(oasysClient.getRiskOfSeriousHarm(crn, assessmentPk, status)).thenReturn(oasysRoshDetails)

        // When
        val roshDetails = oasysAssessmentService.getRosh(crn)

        // Then
        val expectedRoshDetails = RoshDetails(
            assessmentId = assessmentPk,
            assessmentType = assessmentType,
            initiationDate = now,
            dateCompleted = now,
            assessmentStatus = status,
            limitedAccessOffender = false,
            rosh = Rosh(
                riskChildrenCommunity = RiskLevel.LOW,
                riskPrisonersCustody = RiskLevel.MEDIUM,
                riskStaffCustody = RiskLevel.MEDIUM,
                riskStaffCommunity = RiskLevel.MEDIUM,
                riskKnownAdultCustody = null,
                riskKnownAdultCommunity = RiskLevel.MEDIUM,
                riskPublicCustody = RiskLevel.HIGH,
                riskChildrenCustody = RiskLevel.VERY_HIGH
            )
        )
        assertThat(roshDetails).isEqualTo(expectedRoshDetails)
    }

    @Test
    fun `should not attempt to retrieve Health details where CRN does not exist`() {
        // Given
        val crn = "D123456"
        whenever(oasysClient.getAssessmentTimeline(crn)).thenThrow(
            HttpClientErrorException(
                HttpStatus.NOT_FOUND,
                "CRN Not found for $crn"
            )
        )

        // When
        val exception = assertThrows<NotFoundException> {
            oasysAssessmentService.getHealthDetails(crn)
        }

        // Then
        assertThat(exception.message).isEqualTo("404 CRN Not found for $crn")
        verify(never()) { oasysClient.getHealthDetails(any(), any(), any()) }
    }

    @Test
    fun `should return HealthDetails for valid CRN`() {
        // Given
        val oasysHealthDetails = OasysHealthDetails(
            limitedAccessOffender = false,
            listOf(
                OasysHealthAssessment(
                    assessmentPk = assessmentPk,
                    assessmentType = assessmentType,
                    initiationDate = now,
                    assessmentStatus = status,
                    dateCompleted = now,
                    generalHealth = "Yes",
                    alcoholProgramme = "Alcohol misuse - Programme",
                    alcoholEM = "Alcohol misuse - Electronic Monitoring",
                    alcoholCommunity = "Alcohol misuse - Community",
                    interpreterProgramme = "Need for interpreter - Programme",
                    interpreterEM = "Need for interpreter - Electronic Monitoring",
                    interpreterCommunity = "Need for interpreter - Community",
                    communicationProgramme = "Poor communication skills - Programme",
                    communicationEM = "Poor communication skills - Electronic Monitoring",
                    communicationCommunity = "Poor communication skills - Community"
                )
            )
        )
        whenever(oasysClient.getHealthDetails(crn, assessmentPk, status)).thenReturn(oasysHealthDetails)

        // When
        val healthDetails = oasysAssessmentService.getHealthDetails(crn)

        // Then
        val expectedHealthDetails = HealthDetails(
            assessmentId = assessmentPk,
            assessmentType = assessmentType,
            initiationDate = now,
            dateCompleted = now,
            assessmentStatus = status,
            limitedAccessOffender = false,
            health = Health(
                generalHealth = true,
                alcoholMisuse = HealthDetail(
                    "Alcohol misuse - Community",
                    "Alcohol misuse - Electronic Monitoring",
                    "Alcohol misuse - Programme"
                ),
                needForInterpreter = HealthDetail(
                    "Need for interpreter - Community",
                    "Need for interpreter - Electronic Monitoring",
                    "Need for interpreter - Programme"
                ),
                poorCommunicationSkills = HealthDetail(
                    "Poor communication skills - Community",
                    "Poor communication skills - Electronic Monitoring",
                    "Poor communication skills - Programme"
                )
            )
        )
        assertThat(healthDetails).isEqualTo(expectedHealthDetails)
    }
}
