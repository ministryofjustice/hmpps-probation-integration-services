package uk.gov.justice.digital.hmpps.service

import feign.FeignException.NotFound
import feign.Request
import feign.RequestTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
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
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysOffenceAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysOffenceDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysTimelineAssessment
import uk.gov.justice.digital.hmpps.model.Offence
import uk.gov.justice.digital.hmpps.model.OffenceDetails
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class OasysAssessmentServiceTest {

    @Mock
    lateinit var oasysClient: OasysClient
    @InjectMocks
    lateinit var oasysAssessmentService: OasysAssessmentService

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
        val crn = "D123456"
        val assessmentPk = 12345L
        val status = "COMPLETE"
        val assessmentType = "LAYER3"
        val now = ZonedDateTime.now()
        val oasysAssessmentTimeline = OasysAssessmentTimeline(
            crn = crn,
            source = "OASys",
            limitedAccessOffender = false,
            inputs = Inputs(crn, laoPrivilege = "false"),
            timeline = listOf(
                OasysTimelineAssessment(assessmentPk, assessmentType, now, status, now)
            )
        )

        whenever(oasysClient.getAssessmentTimeline(crn)).thenReturn(oasysAssessmentTimeline)

        val oasysOffenceDetails = OasysOffenceDetails(
            limitedAccessOffender = false,
            listOf(OasysOffenceAssessment(
                assessmentPk = assessmentPk,
                assessmentType = assessmentType,
                initiationDate = now,
                assessmentStatus = status,
                offenceAnalysis = "analysis",
                dateCompleted = now
            ))
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
}

