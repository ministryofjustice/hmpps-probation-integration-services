package uk.gov.justice.digital.hmpps.integrations.nomis

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@FeignClient(name = "nomis-client", url = "\${integrations.nomis.url}")
interface NomisClient {
    @GetMapping(value = ["/case-notes/{offenderId}/{caseNoteId}"])
    fun getCaseNote(@PathVariable offenderId: String, @PathVariable caseNoteId: Long): CaseNote?
}