package uk.gov.justice.digital.hmpps.integrations.randm

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import java.net.URI

@FeignClient(name = "refer-and-monitor", url = "https://dummy-url/to/be/overridden")
interface ReferAndMonitorClient {
    @GetMapping
    fun getSession(uri: URI): ReferralSession?
}
