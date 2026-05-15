package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.model.CreateAdjustmentRequest
import uk.gov.justice.digital.hmpps.model.UpdateAdjustmentRequest
import uk.gov.justice.digital.hmpps.service.AdjustmentService
import java.util.*

@RestController
@PreAuthorize("hasRole('PROBATION_API__COMMUNITY_PAYBACK__CASE_DETAIL')")
class AdjustmentsController(
    private val adjustmentService: AdjustmentService
) {
    @GetMapping("/adjustments")
    fun getAdjustments(
        @RequestParam crn: String,
        @RequestParam eventNumber: Int
    ) = adjustmentService.getAdjustments(crn, eventNumber)

    @GetMapping("/adjustments/{reference:[0-9a-fA-F-]{36}}")
    fun getAdjustment(
        @PathVariable reference: UUID
    ) = adjustmentService.getAdjustment(reference)

    @PostMapping("/adjustments")
    fun createAdjustments(
        @RequestParam username: String,
        @RequestBody adjustmentRequests: List<CreateAdjustmentRequest>
    ) = adjustmentService.createAdjustments(adjustmentRequests, username)

    @PutMapping("/adjustments/{reference:[0-9a-fA-F-]{36}}")
    fun updateAdjustments(
        @PathVariable reference: UUID,
        @RequestParam username: String,
        @RequestBody adjustmentRequest: UpdateAdjustmentRequest
    ) = adjustmentService.updateAdjustment(reference, adjustmentRequest, username)

    @DeleteMapping("/adjustments/{reference:[0-9a-fA-F-]{36}}")
    fun deleteAdjustments(
        @PathVariable reference: UUID,
    ) = adjustmentService.deleteAdjustment(reference)
}
