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
    @GetMapping("/{crn}/event/{eventNumber}/adjustments")
    fun getAdjustments(
        @PathVariable crn: String,
        @PathVariable eventNumber: Int) = adjustmentService.getAdjustments(crn, eventNumber)

    @PostMapping("/{crn}/event/{eventNumber}/adjustments")
    fun createAdjustments(
        @PathVariable crn: String,
        @PathVariable eventNumber: Int,
        @RequestParam username: String,
        @RequestBody adjustmentRequest: List<AdjustmentRequest>) = adjustmentService.createAdjustments(
        adjustmentRequest, crn, eventNumber, username)

    @PutMapping("/adjustments/{adjustmentId}")
    fun updateAdjustments(
        @PathVariable adjustmentId: Long,
        @RequestParam username: String,
        @RequestBody adjustmentRequest: AdjustmentRequest
    ) = adjustmentService.updateAdjustment(adjustmentId, adjustmentRequest, username)

    @DeleteMapping("/adjustments/{adjustmentId}")
    fun deleteAdjustments(
        @PathVariable adjustmentId: Long,
        @RequestParam username: String,
        ) = adjustmentService.deleteAdjustment(adjustmentId, username)

}