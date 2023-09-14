package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.services.Identifier
import uk.gov.justice.digital.hmpps.services.ProbationRecordService

@RestController
class ProbationRecordResource(val prService: ProbationRecordService) {
    @PreAuthorize("hasRole('ROLE_MANAGE_POM_CASES')")
    @GetMapping(value = ["/case-records/{identifier}"])
    fun handle(@PathVariable("identifier") identifier: String) = prService.findByIdentifier(Identifier(identifier))
}
