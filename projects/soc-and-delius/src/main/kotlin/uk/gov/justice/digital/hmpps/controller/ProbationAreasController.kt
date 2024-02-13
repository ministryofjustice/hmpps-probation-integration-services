package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Parameter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ProbationAreaService

@RestController
class ProbationAreasController(private val probationAreaService: ProbationAreaService) {
    @PreAuthorize("hasRole('PROBATION_API__SOC__CASE_DETAIL')")
    @GetMapping(value = ["/probation-areas"])
    fun probationAreas(
        @Parameter(description = "Include (true) or exclude (false) any probation areas that are not selectable")
        @RequestParam(defaultValue = "false")
        includeNonSelectable: Boolean = false
    ) = probationAreaService.getProbationAreas(includeNonSelectable)
}
