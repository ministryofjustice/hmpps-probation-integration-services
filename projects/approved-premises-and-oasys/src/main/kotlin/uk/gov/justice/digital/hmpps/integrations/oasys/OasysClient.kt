package uk.gov.justice.digital.hmpps.integrations.oasys

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import uk.gov.justice.digital.hmpps.config.FeignOAuth2Config

@FeignClient(
    name = "ords-oasys",
    url = "\${integrations.ords-oasys.url}",
    configuration = [FeignOAuth2Config::class],
    decode404 = true
)
interface OasysClient {
    @GetMapping(value = ["/ass/timeline/{crn}/ALLOW"])
    fun getAssessmentTimeline(@PathVariable("crn") crn: String): OrdsAssessmentTimeline?
}
