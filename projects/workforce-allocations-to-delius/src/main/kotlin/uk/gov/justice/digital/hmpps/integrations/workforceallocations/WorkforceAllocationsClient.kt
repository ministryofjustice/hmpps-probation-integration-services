package uk.gov.justice.digital.hmpps.integrations.workforceallocations

import org.springframework.web.service.annotation.GetExchange
import java.net.URI

interface WorkforceAllocationsClient {
    @GetExchange
    fun getAllocationDetail(baseUrl: URI): AllocationDetail
}
