package uk.gov.justice.digital.hmpps.controller

import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.StaffService

@RestController
class StaffController(
    private val staffService: StaffService
) {
    @PreAuthorize("hasRole('ROLE_APPROVED_PREMISES_STAFF')")
    @GetMapping(value = ["/approved-premises/{code}/key-workers"])
    fun getStaff(
        @PathVariable("code") code: String,
        pageable: Pageable = Pageable.unpaged()
    ) = staffService.getStaffInApprovedPremises(code, pageable)
}
