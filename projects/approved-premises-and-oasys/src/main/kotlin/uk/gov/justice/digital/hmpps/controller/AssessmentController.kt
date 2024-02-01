package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysTimelineAssessment
import uk.gov.justice.digital.hmpps.model.HealthDetails
import uk.gov.justice.digital.hmpps.model.NeedsDetails
import uk.gov.justice.digital.hmpps.model.OffenceDetails
import uk.gov.justice.digital.hmpps.model.RiskAssessmentDetails
import uk.gov.justice.digital.hmpps.model.RiskManagementPlanDetails
import uk.gov.justice.digital.hmpps.model.RiskToTheIndividualDetails
import uk.gov.justice.digital.hmpps.model.RoshDetails
import uk.gov.justice.digital.hmpps.model.RoshSummaryDetails
import uk.gov.justice.digital.hmpps.service.OasysAssessmentService

@RestController
class AssessmentController(private var oasysAssessmentService: OasysAssessmentService) {
    @PreAuthorize("hasAnyRole('ROLE_APPROVED_PREMISES_ASSESSMENTS','PROBATION_API__APPROVED_PREMISES__ASSESSMENTS')")
    @GetMapping(value = ["/latest-assessment/{crn}"])
    fun getAssessmentTimeline(
        @PathVariable("crn") crn: String
    ): OasysTimelineAssessment {
        return oasysAssessmentService.getLatestAssessment(crn)
    }

    @PreAuthorize("hasAnyRole('ROLE_APPROVED_PREMISES_ASSESSMENTS','PROBATION_API__APPROVED_PREMISES__ASSESSMENTS')")
    @GetMapping(value = ["/offence-details/{crn}"])
    fun getOffenceDetails(
        @PathVariable("crn") crn: String
    ): OffenceDetails {
        return oasysAssessmentService.getOffenceDetails(crn)
    }

    @PreAuthorize("hasAnyRole('ROLE_APPROVED_PREMISES_ASSESSMENTS','PROBATION_API__APPROVED_PREMISES__ASSESSMENTS')")
    @GetMapping(value = ["/needs-details/{crn}"])
    fun getNeeds(
        @PathVariable("crn") crn: String
    ): NeedsDetails {
        return oasysAssessmentService.getNeedsDetails(crn)
    }

    @PreAuthorize("hasAnyRole('ROLE_APPROVED_PREMISES_ASSESSMENTS','PROBATION_API__APPROVED_PREMISES__ASSESSMENTS')")
    @GetMapping(value = ["/risk-management-plan/{crn}"])
    fun getRiskManagementPlan(
        @PathVariable("crn") crn: String
    ): RiskManagementPlanDetails {
        return oasysAssessmentService.getRiskManagementPlanDetails(crn)
    }

    @PreAuthorize("hasAnyRole('ROLE_APPROVED_PREMISES_ASSESSMENTS','PROBATION_API__APPROVED_PREMISES__ASSESSMENTS')")
    @GetMapping(value = ["/rosh-summary/{crn}"])
    fun getRoshSummary(
        @PathVariable("crn") crn: String
    ): RoshSummaryDetails {
        return oasysAssessmentService.getRoshSummary(crn)
    }

    @PreAuthorize("hasAnyRole('ROLE_APPROVED_PREMISES_ASSESSMENTS','PROBATION_API__APPROVED_PREMISES__ASSESSMENTS')")
    @GetMapping(value = ["/risk-to-the-individual/{crn}"])
    fun getRiskToTheIndividual(
        @PathVariable("crn") crn: String
    ): RiskToTheIndividualDetails {
        return oasysAssessmentService.getRiskToIndividual(crn)
    }

    @PreAuthorize("hasAnyRole('ROLE_APPROVED_PREMISES_ASSESSMENTS','PROBATION_API__APPROVED_PREMISES__ASSESSMENTS')")
    @GetMapping(value = ["/risk-assessment/{crn}"])
    fun getRiskAssessment(
        @PathVariable("crn") crn: String
    ): RiskAssessmentDetails {
        return oasysAssessmentService.getRiskAssessment(crn)
    }

    @PreAuthorize("hasAnyRole('ROLE_APPROVED_PREMISES_ASSESSMENTS','PROBATION_API__APPROVED_PREMISES__ASSESSMENTS')")
    @GetMapping(value = ["/rosh/{crn}"])
    fun getRosh(
        @PathVariable("crn") crn: String
    ): RoshDetails {
        return oasysAssessmentService.getRosh(crn)
    }

    @PreAuthorize("hasAnyRole('ROLE_APPROVED_PREMISES_ASSESSMENTS','PROBATION_API__APPROVED_PREMISES__ASSESSMENTS')")
    @GetMapping(value = ["/health-details/{crn}"])
    fun getHealthDetails(
        @PathVariable("crn") crn: String
    ): HealthDetails {
        return oasysAssessmentService.getHealthDetails(crn)
    }
}
