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

    @Deprecated("Pass a reference instead of an internal id", replaceWith = ReplaceWith("getAdjustment"))
    @GetMapping("/adjustments/{id:\\d+}")
    fun getAdjustmentByInternalId(
        @PathVariable id: Long
    ) = adjustmentService.getAdjustment(id)

    @GetMapping("/adjustments/{reference:[0-9a-fA-F-]{36}}")
    fun getAdjustment(
        @PathVariable reference: UUID
    ) = adjustmentService.getAdjustment(reference)

    @PostMapping("/adjustments")
    fun createAdjustments(
        @RequestParam username: String,
        @RequestBody adjustmentRequests: List<CreateAdjustmentRequest>
    ) = adjustmentService.createAdjustments(
        adjustmentRequests, username
    )

    @Deprecated("Pass a reference instead of an internal id", replaceWith = ReplaceWith("updateAdjustments"))
    @PutMapping("/adjustments/{id:\\d+}")
    fun updateAdjustmentsByInternalId(
        @PathVariable id: Long,
        @RequestParam username: String,
        @RequestBody adjustmentRequest: UpdateAdjustmentRequest
    ) = adjustmentService.updateAdjustment(id, adjustmentRequest, username)

    @PutMapping("/adjustments/{reference:[0-9a-fA-F-]{36}}")
    fun updateAdjustments(
        @PathVariable reference: UUID,
        @RequestParam username: String,
        @RequestBody adjustmentRequest: UpdateAdjustmentRequest
    ) = adjustmentService.updateAdjustment(reference, adjustmentRequest, username)

    @DeleteMapping("/adjustments/{id:\\d+}")
    fun deleteAdjustments(
        @PathVariable id: Long,
    ) = adjustmentService.deleteAdjustment(id)

    @DeleteMapping("/adjustments/{reference:[0-9a-fA-F-]{36}}")
    fun deleteAdjustments(
        @PathVariable reference: UUID,
    ) = adjustmentService.deleteAdjustment(reference)
}
