package uk.gov.justice.digital.hmpps.integrations.oasys.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import uk.gov.justice.digital.hmpps.config.FeignOAuth2Config
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysNeedsDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysOffenceDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskManagementPlanDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRoshSummary

@FeignClient(
    name = "ords-oasys",
    url = "\${integrations.ords-oasys.url}",
    configuration = [FeignOAuth2Config::class]
)
interface OasysClient {
    @GetMapping(value = ["/ap/asslist/{crn}/ALLOW"])
    fun getAssessmentTimeline(@PathVariable("crn") crn: String): OasysAssessmentTimeline

    @GetMapping(value = ["/ap/offence/{crn}/ALLOW/{assessmentId}/{status}"])
    fun getOffenceDetails(
        @PathVariable("crn") crn: String,
        @PathVariable("assessmentId") assessmentId: Long,
        @PathVariable("status") status: String,
    ): OasysOffenceDetails

    @GetMapping(value = ["/ap/needs/{crn}/ALLOW/{assessmentId}/{status}"])
    fun getNeedsDetails(
        @PathVariable("crn") crn: String,
        @PathVariable("assessmentId") assessmentId: Long,
        @PathVariable("status") status: String,
    ): OasysNeedsDetails

    @GetMapping(value = ["/ap/rmp/{crn}/ALLOW/{assessmentId}/{status}"])
    fun getRiskManagementPlanDetails(
        @PathVariable("crn") crn: String,
        @PathVariable("assessmentId") assessmentId: Long,
        @PathVariable("status") status: String,
    ): OasysRiskManagementPlanDetails

    @GetMapping(value = ["/ap/roshsum/{crn}/ALLOW/{assessmentId}/{status}"])
    fun getRoshSummary(
        @PathVariable("crn") crn: String,
        @PathVariable("assessmentId") assessmentId: Long,
        @PathVariable("status") status: String,
    ): OasysRoshSummary
}
