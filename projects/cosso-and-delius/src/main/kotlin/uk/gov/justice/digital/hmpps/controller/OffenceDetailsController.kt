package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.OffenceService

@RestController
class OffenceDetailsController( val offenceService: OffenceService) {
    @PreAuthorize("hasRole('PROBATION_API__COSSO__CASE_DETAILS')")
    @GetMapping(value = ["/offence-details/{uuid}"])
    fun getOffenceDetails(@PathVariable uuid: String) = offenceService.getOffenceDetails(uuid)
}