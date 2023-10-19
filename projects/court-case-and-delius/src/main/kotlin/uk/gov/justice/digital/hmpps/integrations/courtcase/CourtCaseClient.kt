package uk.gov.justice.digital.hmpps.integrations.courtcase

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import java.net.URI

@FeignClient(name = "court-case", url = "https://dummy-url/to/be/overridden")
interface CourtCaseClient {
    @GetMapping
    fun getCourtCaseNote(baseUrl: URI): CourtCaseNote?
}
