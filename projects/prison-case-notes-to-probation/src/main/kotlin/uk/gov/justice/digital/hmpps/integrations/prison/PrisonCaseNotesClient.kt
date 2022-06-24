package uk.gov.justice.digital.hmpps.integrations.prison

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "case-notes", url = "\${integrations.prison-case-notes.url}")
interface PrisonCaseNotesClient {
    @GetMapping(value = ["/case-notes/{offenderId}/{caseNoteId}"])
    fun getCaseNote(@PathVariable offenderId: String, @PathVariable caseNoteId: Long): PrisonCaseNote?
}
