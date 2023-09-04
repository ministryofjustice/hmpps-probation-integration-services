package uk.gov.justice.digital.hmpps.integrations.oasys

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import uk.gov.justice.digital.hmpps.config.FeignOAuth2Config

@FeignClient(
    name = "ords-oasys",
    url = "\${integrations.ords-oasys.url}",
    configuration = [FeignOAuth2Config::class]
)
interface OrdsClient {

    @GetMapping("/ass/offence/{crn}/{lao}")
    fun getOffenceDetails(
        @PathVariable("crn") crn: String,
        @PathVariable("lao") lao: String
    ): OasysResponse<OffenceDetails>?

    @GetMapping("/ass/rmp/{crn}/{lao}")
    fun getRiskManagementPlans(
        @PathVariable("crn") crn: String,
        @PathVariable("lao") lao: String
    ): OasysResponse<RiskManagementPlan>?

    @GetMapping("/ass/allrisk/{crn}/{lao}")
    fun getRiskPredictors(
        @PathVariable("crn") crn: String,
        @PathVariable("lao") lao: String
    ): OasysResponse<RiskPredictors>?

    @GetMapping("/ass/timeline/{crn}/{lao}")
    fun getTimeline(@PathVariable("crn") crn: String, @PathVariable("lao") lao: String): OasysTimelineResponse?
}
