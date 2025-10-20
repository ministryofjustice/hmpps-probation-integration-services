package uk.gov.justice.digital.hmpps.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.TeamService

@RestController
@RequestMapping("/providers")
class ProvidersController(
    private val teamService: TeamService
) {
    @GetMapping(value = ["/{code}/teams"])
    fun getTeams(@PathVariable code: String) = teamService.getUnpaidWorkTeams(code)
}
