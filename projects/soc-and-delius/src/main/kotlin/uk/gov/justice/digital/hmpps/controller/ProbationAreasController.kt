package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
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

    @PreAuthorize("hasRole('PROBATION_API__SOC__CASE_DETAIL')")

    @PostMapping(value = ["/probation-area-history"])
    fun probationAreas(
        @RequestBody @Size(min = 1, max = 500, message = "Please provide between 1 and 500 CRNs") crns: List<String>
    ) = probationAreaService.getProbationAreaHistory(crns)
}
