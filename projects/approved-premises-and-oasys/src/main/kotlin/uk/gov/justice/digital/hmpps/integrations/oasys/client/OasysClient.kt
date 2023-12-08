package uk.gov.justice.digital.hmpps.integrations.oasys.client

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysHealthDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysNeedsDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysOffenceDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskAssessmentDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskManagementPlanDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskToTheIndividualDetails
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRoshAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRoshSummary

interface OasysClient {
    @GetExchange(url = "/ap/asslist/{crn}/ALLOW")
    fun getAssessmentTimeline(
        @PathVariable("crn") crn: String,
    ): OasysAssessmentTimeline

    @GetExchange(url = "/ap/offence/{crn}/ALLOW/{assessmentId}/{status}")
    fun getOffenceDetails(
        @PathVariable("crn") crn: String,
        @PathVariable("assessmentId") assessmentId: Long,
        @PathVariable("status") status: String,
    ): OasysOffenceDetails

    @GetExchange(url = "/ap/needs/{crn}/ALLOW/{assessmentId}/{status}")
    fun getNeedsDetails(
        @PathVariable("crn") crn: String,
        @PathVariable("assessmentId") assessmentId: Long,
        @PathVariable("status") status: String,
    ): OasysNeedsDetails

    @GetExchange(url = "/ap/rmp/{crn}/ALLOW/{assessmentId}/{status}")
    fun getRiskManagementPlanDetails(
        @PathVariable("crn") crn: String,
        @PathVariable("assessmentId") assessmentId: Long,
        @PathVariable("status") status: String,
    ): OasysRiskManagementPlanDetails

    @GetExchange(url = "/ap/roshsum/{crn}/ALLOW/{assessmentId}/{status}")
    fun getRoshSummary(
        @PathVariable("crn") crn: String,
        @PathVariable("assessmentId") assessmentId: Long,
        @PathVariable("status") status: String,
    ): OasysRoshSummary

    @GetExchange(url = "/ap/riskind/{crn}/ALLOW/{assessmentId}/{status}")
    fun getRiskToTheIndividual(
        @PathVariable("crn") crn: String,
        @PathVariable("assessmentId") assessmentId: Long,
        @PathVariable("status") status: String,
    ): OasysRiskToTheIndividualDetails

    @GetExchange(url = "/ap/riskass/{crn}/ALLOW/{assessmentId}/{status}")
    fun getRiskAssessment(
        @PathVariable("crn") crn: String,
        @PathVariable("assessmentId") assessmentId: Long,
        @PathVariable("status") status: String,
    ): OasysRiskAssessmentDetails

    @GetExchange(url = "/ap/rosh/{crn}/ALLOW/{assessmentId}/{status}")
    fun getRiskOfSeriousHarm(
        @PathVariable("crn") crn: String,
        @PathVariable("assessmentId") assessmentId: Long,
        @PathVariable("status") status: String,
    ): OasysRoshAssessment

    @GetExchange(url = "/ap/health/{crn}/ALLOW/{assessmentId}/{status}")
    fun getHealthDetails(
        @PathVariable("crn") crn: String,
        @PathVariable("assessmentId") assessmentId: Long,
        @PathVariable("status") status: String,
    ): OasysHealthDetails
}
