package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.Teams
import uk.gov.justice.digital.hmpps.service.TeamsService

@RestController
@RequestMapping("/user")
class UserController(private val teamsService: TeamsService) {
    @PreAuthorize("hasRole('PROBATION_API__EMDI__REFERENCE_DATA')")
    @GetMapping("/{username}/teams")
    fun getTeamsByUsername(@PathVariable username: String): Teams =
        teamsService.getTeamsForUser(username)
}