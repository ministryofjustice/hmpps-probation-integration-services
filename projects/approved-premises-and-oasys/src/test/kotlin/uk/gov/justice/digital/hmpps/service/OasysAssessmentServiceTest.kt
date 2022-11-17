package uk.gov.justice.digital.hmpps.service

import feign.FeignException.NotFound
import feign.Request
import feign.RequestTemplate
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
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.oasys.client.OasysClient
import uk.gov.justice.digital.hmpps.integrations.oasys.model.Inputs
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysNeedsAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysNeedsDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysOffenceAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysOffenceDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskManagementPlanAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskManagementPlanDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysTimelineAssessment
import uk.gov.justice.digital.hmpps.model.Needs
import uk.gov.justice.digital.hmpps.model.NeedsDetails
import uk.gov.justice.digital.hmpps.model.Offence
import uk.gov.justice.digital.hmpps.model.OffenceDetails
import uk.gov.justice.digital.hmpps.model.RiskManagementPlan
import uk.gov.justice.digital.hmpps.model.RiskManagementPlanDetails
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
        val request = Request.create(
            Request.HttpMethod.GET, "url",
            HashMap(), null, RequestTemplate()
        )
        whenever(oasysClient.getAssessmentTimeline(crn)).thenThrow(NotFound("CRN Not found for $crn", request, null, null))

        // When
        val exception = assertThrows<NotFoundException> {
            oasysAssessmentService.getOffenceDetails(crn)
        }

        // Then
        assertThat(exception.message).isEqualTo("CRN Not found for $crn")
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
                    dateCompleted = now
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
        val request = Request.create(
            Request.HttpMethod.GET, "url",
            HashMap(), null, RequestTemplate()
        )
        whenever(oasysClient.getAssessmentTimeline(crn)).thenThrow(NotFound("CRN Not found for $crn", request, null, null))

        // When
        val exception = assertThrows<NotFoundException> {
            oasysAssessmentService.getNeedsDetails(crn)
        }

        // Then
        assertThat(exception.message).isEqualTo("CRN Not found for $crn")
        verify(never()) { oasysClient.getNeedsDetails(any(), any(), any()) }
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
            Needs(
                offenceAnalysisDetails = "analysis"
            )
        )
        assertThat(needsDetails).isEqualTo(expectedNeedsDetails)
    }

    @Test
    fun `should not attempt to retrieve risk management plan details where CRN does not exist`() {
        // Given
        val crn = "D123456"
        val request = Request.create(
            Request.HttpMethod.GET, "url",
            HashMap(), null, RequestTemplate()
        )
        whenever(oasysClient.getAssessmentTimeline(crn)).thenThrow(NotFound("CRN Not found for $crn", request, null, null))

        // When
        val exception = assertThrows<NotFoundException> {
            oasysAssessmentService.getRiskManagementPlanDetails(crn)
        }

        // Then
        assertThat(exception.message).isEqualTo("CRN Not found for $crn")
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
        whenever(oasysClient.getRiskManagementPlanDetails(crn, assessmentPk, status)).thenReturn(oasysRiskManagementPlanDetails)

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
}
