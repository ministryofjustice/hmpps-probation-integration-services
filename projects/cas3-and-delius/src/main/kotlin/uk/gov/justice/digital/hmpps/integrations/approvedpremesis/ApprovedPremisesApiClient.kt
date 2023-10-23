package uk.gov.justice.digital.hmpps.integrations.approvedpremesis

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import uk.gov.justice.digital.hmpps.config.FeignOAuth2Config
import java.net.URI

@FeignClient(
    name = "approved-premises-api",
    url = "https://dummy-url/to/be/overridden",
    configuration = [FeignOAuth2Config::class]
)
interface ApprovedPremisesApiClient {
    @GetMapping fun getApplicationSubmittedDetails(uri: URI): EventDetails<ApplicationSubmitted>
}
