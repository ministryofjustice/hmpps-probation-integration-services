package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.DefendantDetails
import uk.gov.justice.digital.hmpps.service.DetailsService

@RestController
class DefendantDetailsController(
    private val detailsService: DetailsService
) {
    @PreAuthorize("hasRole('PROBATION_API__PSR__CONTEXT')")
    @GetMapping("/report/{psrUuid}/defendant-details")
    fun getDefendantDetails(@PathVariable psrUuid: String): DefendantDetails =
        detailsService.getDefendantDetails(psrUuid)
}