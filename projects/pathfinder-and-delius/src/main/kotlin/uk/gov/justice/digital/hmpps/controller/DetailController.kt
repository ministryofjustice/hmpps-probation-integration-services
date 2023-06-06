package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.BatchRequest
import uk.gov.justice.digital.hmpps.service.DetailService

@RestController
class DetailController(private val detailService: DetailService) {

    @PreAuthorize("hasRole('ROLE_PATHFINDER_PROBATION_CASE')")
    @GetMapping(value = ["/detail/{value}"])
    fun details(
        @PathVariable value: String,
        @RequestParam(required = false, defaultValue = "CRN") type: IdentifierType
    ) = detailService.getDetails(value, type)

    @PreAuthorize("hasRole('ROLE_PATHFINDER_PROBATION_CASE')")
    @PostMapping(value = ["/detail"])
    fun batchDetails(
        @Valid @RequestBody
        request: BatchRequest
    ) = detailService.getBatchDetails(request)
}
