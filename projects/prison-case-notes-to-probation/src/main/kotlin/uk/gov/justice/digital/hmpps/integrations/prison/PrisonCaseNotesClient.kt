package uk.gov.justice.digital.hmpps.integrations.prison

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import uk.gov.justice.digital.hmpps.config.CaseNotesFeignConfig
import java.net.URI

@FeignClient(name = "prison-case-notes", configuration = [CaseNotesFeignConfig::class])
interface PrisonCaseNotesClient {
    @GetMapping
    fun getCaseNote(baseUrl: URI): PrisonCaseNote?
}
