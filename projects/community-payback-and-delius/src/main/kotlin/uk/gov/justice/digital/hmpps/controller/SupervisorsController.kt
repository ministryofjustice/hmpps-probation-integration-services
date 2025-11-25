package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ProvidersService

@RestController
@RequestMapping("/supervisors")
@PreAuthorize("hasRole('PROBATION_API__COMMUNITY_PAYBACK__CASE_DETAIL')")
class SupervisorsController(
    private val providersService: ProvidersService
) {
    @GetMapping
    fun getSupervisors(@RequestParam username: String) = providersService.getSupervisorsForUsername(username)
}
