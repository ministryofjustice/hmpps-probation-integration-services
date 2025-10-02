package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.model.UserTeams
import uk.gov.justice.digital.hmpps.service.TeamService
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
@Tag(name = "Users")
@PreAuthorize("hasRole('PROBATION_API__ACCREDITED_PROGRAMMES__CASE_DETAIL')")
class UserController(private val userAccessService: UserAccessService, private val teamService: TeamService) {
    @PostMapping(value = ["/user/{username}/access"])
    fun getLimitedAccess(
        @PathVariable username: String,
        @Size(min = 1, max = 500, message = "Please provide between 1 and 500 crns") @RequestBody crns: List<String>
    ) = userAccessService.userAccessFor(username, crns)

    @GetMapping("/user/{username}/teams")
    fun getUserRegionInformation(@PathVariable username: String): UserTeams =
        UserTeams(teamService.findUserTeams(username))
}
