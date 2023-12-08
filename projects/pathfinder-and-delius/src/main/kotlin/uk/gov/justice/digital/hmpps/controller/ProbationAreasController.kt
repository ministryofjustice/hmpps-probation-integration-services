package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ProbationAreaService

@RestController
class ProbationAreasController(private val probationAreaService: ProbationAreaService) {
    @PreAuthorize("hasRole('ROLE_PATHFINDER_PROBATION_CASE')")
    @GetMapping(value = ["/probation-areas"])
    fun probationAreas() = probationAreaService.getProbationAreas()
}
