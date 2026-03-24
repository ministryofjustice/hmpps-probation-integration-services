package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.AdjustmentRequest
import uk.gov.justice.digital.hmpps.service.AdjustmentService

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

    @GetMapping("/adjustments/{id}")
    fun getAdjustment(
        @PathVariable id: Long
    ) = adjustmentService.getAdjustment(id)

    @PostMapping("/adjustments")
    fun createAdjustments(
        @RequestParam username: String,
        @RequestBody adjustmentRequests: List<AdjustmentRequest>
    ) = adjustmentService.createAdjustments(
        adjustmentRequests, username
    )

    @PutMapping("/adjustments/{adjustmentId}")
    fun updateAdjustments(
        @PathVariable adjustmentId: Long,
        @RequestParam username: String,
        @RequestBody adjustmentRequest: AdjustmentRequest
    ) = adjustmentService.updateAdjustment(adjustmentId, adjustmentRequest, username)

    @DeleteMapping("/adjustments/{adjustmentId}")
    fun deleteAdjustments(
        @PathVariable adjustmentId: Long,
    ) = adjustmentService.deleteAdjustment(adjustmentId)
}