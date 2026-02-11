package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.OffenceDetails
import uk.gov.justice.digital.hmpps.service.OffenceService

@RestController
class OffenceController(private val offenceService: OffenceService) {
    @PreAuthorize("hasRole('PROBATION_API__PSR__CONTEXT')")
    @GetMapping("/case/{crn}/event/{event}/offences")
    fun getOffences(
        @PathVariable(name = "crn") crn: String,
        @PathVariable(name = "event") event: Int
    ): OffenceDetails =
        offenceService.getOffences(crn, event)
}