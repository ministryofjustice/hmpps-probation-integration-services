package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.ManagedOffender
import uk.gov.justice.digital.hmpps.service.TeamService

@RestController
@RequestMapping("team")
@PreAuthorize("hasRole('PROBATION_API__ASSESS_FOR_EARLY_RELEASE__CASE_DETAIL')")
class TeamResource(private val teamService: TeamService) {
    @GetMapping("/{teamCode}/caseload/managed-offenders")
    fun getManagedOffendersByTeam(@PathVariable teamCode: String): List<ManagedOffender> =
        teamService.getManagedOffendersByTeam(teamCode)
}
