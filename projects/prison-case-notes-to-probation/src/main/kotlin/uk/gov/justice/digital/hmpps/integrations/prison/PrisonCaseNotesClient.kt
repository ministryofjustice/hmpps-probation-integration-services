package uk.gov.justice.digital.hmpps.integrations.prison

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import java.net.URI

@FeignClient(name = "prison-case-notes", url = "https://dummy-url/to/be/overridden")
interface PrisonCaseNotesClient {
    @GetMapping
    fun getCaseNote(baseUrl: URI): PrisonCaseNote?
}
