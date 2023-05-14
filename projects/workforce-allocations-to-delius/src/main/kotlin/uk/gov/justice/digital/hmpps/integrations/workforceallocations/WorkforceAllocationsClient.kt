package uk.gov.justice.digital.hmpps.integrations.workforceallocations

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import java.net.URI

@FeignClient(name = "workforce-allocations", url = "https://dummy-url/to/be/overridden")
fun interface WorkforceAllocationsClient {
    @GetMapping
    fun getAllocationDetail(baseUrl: URI): AllocationDetail
}
