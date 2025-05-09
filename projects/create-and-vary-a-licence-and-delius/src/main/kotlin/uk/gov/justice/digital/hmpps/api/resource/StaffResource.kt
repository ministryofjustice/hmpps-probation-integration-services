package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.service.StaffService

@RestController
@RequestMapping("staff")
@PreAuthorize("hasRole('PROBATION_API__CVL__CASE_DETAIL')")
class StaffResource(
    private val staffService: StaffService
) {
    @GetMapping("/{username}")
    fun findStaff(@PathVariable username: String): Staff = staffService.findStaff(username)

    @GetMapping("/byid/{id}")
    fun findStaff(@PathVariable id: Long): Staff = staffService.findStaffById(id)

    @GetMapping("/bycode/{code}")
    fun findStaffByCode(@PathVariable code: String): Staff = staffService.findStaffByCode(code)

    @GetMapping("/{boroughCode}/pdu-head")
    fun findPDUHead(@PathVariable boroughCode: String): List<PDUHead> = staffService.findPDUHeads(boroughCode)

    @PostMapping
    fun findStaffForUsernames(@RequestBody usernames: List<String>): List<StaffName> =
        staffService.findStaffForUsernames(
            usernames
        )

    @GetMapping("/byid/{id}/caseload/managed-offenders")
    fun getManagedOffenders(@PathVariable id: Long): List<ManagedOffender> =
        staffService.getManagedOffendersByStaffId(id)

    @PostMapping("/byid/{id}/caseload/team-managed-offenders")
    fun getManagedOffendersForTeam(
        @PathVariable id: Long,
        @RequestBody body: SearchRequest?,
        @PageableDefault(page = 0, size = 100, sort = ["firstName", "surname"]) page: Pageable
    ): PagedModel<ManagedOffender> = staffService.getTeamManagedOffendersByStaffId(id, body?.query, page)

    @GetMapping("/{staffCode}/caseload/managed-offenders")
    fun getManagedOffenders(@PathVariable staffCode: String): List<ManagedOffender> =
        staffService.getManagedOffenders(staffCode)
}
