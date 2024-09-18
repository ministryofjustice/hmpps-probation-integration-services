package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.ManagedOffender
import uk.gov.justice.digital.hmpps.api.model.PDUHead
import uk.gov.justice.digital.hmpps.api.model.Staff
import uk.gov.justice.digital.hmpps.api.model.StaffName
import uk.gov.justice.digital.hmpps.service.StaffService

@RestController
@RequestMapping("staff")
class StaffResource(
    private val staffService: StaffService
) {
    @PreAuthorize("hasRole('PROBATION_API__CVL__CASE_DETAIL')")
    @GetMapping("/{username}")
    fun findStaff(@PathVariable username: String): Staff = staffService.findStaff(username)

    @PreAuthorize("hasRole('PROBATION_API__CVL__CASE_DETAIL')")
    @GetMapping("/byid/{id}")
    fun findStaff(@PathVariable id: Long): Staff = staffService.findStaffById(id)

    @PreAuthorize("hasRole('PROBATION_API__CVL__CASE_DETAIL')")
    @GetMapping("/{boroughCode}/pdu-head")
    fun findPDUHead(@PathVariable boroughCode: String): List<PDUHead> = staffService.findPDUHeads(boroughCode)

    @PreAuthorize("hasRole('PROBATION_API__CVL__CASE_DETAIL')")
    @PostMapping
    fun findStaffForUsernames(@RequestBody usernames: List<String>): List<StaffName> =
        staffService.findStaffForUsernames(
            usernames
        )

    @PreAuthorize("hasRole('PROBATION_API__CVL__CASE_DETAIL')")
    @GetMapping("/{staffCode}/caseload/managed-offenders")
    fun getManagedOffenders(@PathVariable staffCode: String): List<ManagedOffender> =
        staffService.getManagedOffenders(staffCode)
}
