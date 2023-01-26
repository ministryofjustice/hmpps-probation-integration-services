package uk.gov.justice.digital.hmpps.controller

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
    @PreAuthorize("hasRole('ROLE_APPROVED_PREMISES_STAFF')")
    @GetMapping(value = ["/teams/managingCase/{crn}"])
    fun getTeamsManagingCase(
        @PathVariable crn: String,
        @RequestParam staffCode: String?
    ) = teamService.getTeamsManagingCase(crn, staffCode)
}
