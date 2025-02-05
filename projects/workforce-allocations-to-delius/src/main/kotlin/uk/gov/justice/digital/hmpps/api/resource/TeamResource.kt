package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.TeamService

@RestController
@RequestMapping("/teams")
@PreAuthorize("hasRole('PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
class TeamResource(private val teamService: TeamService) {
    @GetMapping
    fun activeCases(@RequestParam("teamCode", defaultValue = "") teamCodes: List<String> = listOf()) =
        teamService.getTeams(teamCodes)
}
