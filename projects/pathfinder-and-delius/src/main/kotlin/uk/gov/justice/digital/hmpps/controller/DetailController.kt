package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.BatchRequest
import uk.gov.justice.digital.hmpps.service.DetailService

@Validated
@RestController
class DetailController(private val detailService: DetailService) {
    @PreAuthorize("hasRole('ROLE_PATHFINDER_PROBATION_CASE')")
    @PostMapping(value = ["/detail"])
    fun batchDetails(
        @Valid @RequestBody
        request: BatchRequest,
    ) = detailService.getBatchDetails(request)
}
