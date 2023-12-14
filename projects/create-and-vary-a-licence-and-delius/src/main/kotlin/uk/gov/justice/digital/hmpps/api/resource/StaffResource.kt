package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.PDUHead
import uk.gov.justice.digital.hmpps.api.model.Staff
import uk.gov.justice.digital.hmpps.api.model.StaffName
import uk.gov.justice.digital.hmpps.service.StaffService

@RestController
@RequestMapping("staff")
class StaffResource(
    private val staffService: StaffService
) {
    @PreAuthorize("hasRole('CVL_CONTEXT')")
    @GetMapping("/{username}")
    fun findStaff(@PathVariable username: String): Staff = staffService.findStaff(username)

    @PreAuthorize("hasRole('CVL_CONTEXT')")
    @GetMapping("/{boroughCode}/pdu-head")
    fun findPDUHead(@PathVariable boroughCode: String): List<PDUHead> = staffService.findPDUHeads(boroughCode)

    @PreAuthorize("hasRole('CVL_CONTEXT')")
    @PostMapping
    fun findStaffForUsernames(@RequestBody usernames: List<String>): List<StaffName> =
        staffService.findStaffForUsernames(
            usernames
        )
}
