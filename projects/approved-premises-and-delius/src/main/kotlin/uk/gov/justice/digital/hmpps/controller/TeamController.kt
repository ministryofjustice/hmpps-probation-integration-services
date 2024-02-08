package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.TeamService

@RestController
class TeamController(
    private val teamService: TeamService
) {
    @PreAuthorize("hasRole('PROBATION_API__APPROVED_PREMISES__CASE_DETAIL')")
    @Operation(
        summary = "List all teams involved in managing the probation case",
        description = """A probation case may be managed by staff members who
            are associated with multiple teams in Delius. Approve Premises users
            need to view all applications that may be covered by direct colleagues
            in order to cover absence and other workload-related issues. Respond
            with a list of every team code that covers the probation case identified
            by the CRN to enable the Approved Premises service to present the
            relevant list of Approved Premises applications to it's users.
        """
    )
    @GetMapping(value = ["/teams/managingCase/{crn}"])
    fun getTeamsManagingCase(
        @PathVariable crn: String,
        @RequestParam staffCode: String?
    ) = teamService.getTeamsManagingCase(crn, staffCode)
}
