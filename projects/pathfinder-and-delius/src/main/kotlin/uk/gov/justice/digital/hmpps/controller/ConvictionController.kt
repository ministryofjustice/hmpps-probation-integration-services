package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.BatchRequest
import uk.gov.justice.digital.hmpps.service.ConvictionService

@Validated
@RestController
class ConvictionController(private val convictionService: ConvictionService) {

    @PreAuthorize("hasAnyRole('ROLE_PATHFINDER_PROBATION_CASE','PROBATION_API__PATHFINDER__CASE_DETAIL')")
    @PostMapping(value = ["/convictions"])
    fun convictions(
        @Valid @RequestBody
        request: BatchRequest
    ) = convictionService.getConvictions(request)
}
