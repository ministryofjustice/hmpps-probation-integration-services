package uk.gov.justice.digital.hmpps.integrations.casenotes

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "case-notes", url = "\${integrations.case-notes.url}")
interface CaseNotesClient {
    @GetMapping(value = ["/case-notes/{offenderId}/{caseNoteId}"])
    fun getCaseNote(@PathVariable offenderId: String, @PathVariable caseNoteId: Long): NomisCaseNote?
}
