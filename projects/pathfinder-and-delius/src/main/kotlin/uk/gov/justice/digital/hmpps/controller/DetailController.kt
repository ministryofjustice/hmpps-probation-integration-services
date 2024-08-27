package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.DetailService

@RestController
class DetailController(private val detailService: DetailService) {
    @PreAuthorize("hasRole('PROBATION_API__PATHFINDER__CASE_DETAIL')")
    @GetMapping(value = ["/detail/{value}"])
    fun convictions(
        @PathVariable value: String,
        @RequestParam(required = false, defaultValue = "CRN") type: IdentifierType
    ) = detailService.getDetails(value, type)
}
