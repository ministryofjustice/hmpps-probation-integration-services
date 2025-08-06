package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.BasicDetails
import uk.gov.justice.digital.hmpps.service.DetailsService

@RestController
class SuicideRiskFormController(private val detailsService: DetailsService) {
    @PreAuthorize("hasRole('EXAMPLE')")
    @GetMapping(value = ["/basic-details/{crn}"])
    fun getBasicDetails(@PathVariable crn: String): BasicDetails = detailsService.basicDetails(crn)
}
