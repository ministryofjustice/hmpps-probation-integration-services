package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.oasys.OrdsClient

@Service
class AssessmentService(private val ordsClient: OrdsClient) {
    fun getOffenceDetails(crn: String, lao: String) = ordsClient.getOffenceDetails(crn, lao)
    fun getRiskManagementPlans(crn: String, lao: String) = ordsClient.getRiskManagementPlans(crn, lao)
    fun getRiskPredictors(crn: String, lao: String) = ordsClient.getRiskPredictors(crn, lao)
}
