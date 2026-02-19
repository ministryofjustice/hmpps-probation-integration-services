package uk.gov.justice.digital.hmpps.controller

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.service.ProvidersService
import uk.gov.justice.digital.hmpps.utils.Extensions.mapSorts
import java.time.LocalDate

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

    @GetMapping(value = ["/{providerCode}/teams/{teamCode}/projects"])
    fun getProjects(
        @PathVariable providerCode: String,
        @PathVariable teamCode: String,
        @RequestParam typeCode: List<String> = emptyList(),
        @PageableDefault(page = 0, size = 10, sort = ["name"]) pageable: Pageable
    ) = providersService.getProjectsForTeam(
        teamCode, typeCode, pageable.mapSorts(
            "name" to "lower(project.name)",
            "overdueOutcomesCount" to "coalesce(appointment_stats.overdue_count, 0)",
            "oldestOverdueInDays" to "coalesce(appointment_stats.overdue_days, 0)"
        )
    )

    @GetMapping(value = ["/{providerCode}/teams/{teamCode}/sessions"])
    fun getSessions(
        @PathVariable providerCode: String,
        @PathVariable teamCode: String,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
    ) = providersService.getSessions(teamCode, startDate, endDate)
}
