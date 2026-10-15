package uk.gov.justice.digital.hmpps.controller

import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.model.CreateAdjustmentRequest
import uk.gov.justice.digital.hmpps.model.UpdateAdjustmentRequest
import uk.gov.justice.digital.hmpps.service.AdjustmentService
import java.time.LocalDate
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

    @GetMapping("/adjustments/search")
    fun searchAdjustments(
        @RequestParam(required = false) crn: String?,
        @RequestParam(required = false) eventNumber: Int?,
        @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false) type: AdjustmentType?,
        @RequestParam(required = false) reason: String?,
        @RequestParam(required = false) minutes: Int?,
        pageable: Pageable
    ) = adjustmentService.searchAdjustments(
        crn = crn,
        eventNumber = eventNumber,
        date = date,
        type = type,
        reason = reason,
        minutes = minutes,
        pageable = org.springframework.data.domain.PageRequest.of(
            pageable.pageNumber,
            pageable.pageSize,
            org.springframework.data.domain.Sort.by(pageable.sort.toList().map { order ->
                when (order.property) {
                    "minutes" -> order.withProperty("amount")
                    "reference" -> order.withProperty("externalReference")
                    else -> order
                }
            })
        )
    )
}
