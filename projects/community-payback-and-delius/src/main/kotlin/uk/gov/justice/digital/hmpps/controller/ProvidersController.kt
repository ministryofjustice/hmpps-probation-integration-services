package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.service.ProvidersService

@RestController
@RequestMapping("/providers")
@PreAuthorize("hasRole('PROBATION_API__COMMUNITY_PAYBACK__CASE_DETAIL')")
class ProvidersController(
    private val providersService: ProvidersService
) {
    @GetMapping
    fun getProviders(@RequestParam username: String) = providersService.getProvidersForUser(username)

    @GetMapping(value = ["/{code}/teams"])
    fun getTeams(@PathVariable code: String) = providersService.getUnpaidWorkTeams(code)

    @GetMapping(value = ["/{providerCode}/teams/{teamCode}/supervisors"])
    fun getSupervisors(@PathVariable providerCode: String, @PathVariable teamCode: String) =
        providersService.getSupervisorsForTeam(teamCode)
}
