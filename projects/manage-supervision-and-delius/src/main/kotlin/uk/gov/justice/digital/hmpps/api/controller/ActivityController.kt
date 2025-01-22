package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivitySearchRequest
import uk.gov.justice.digital.hmpps.service.ActivityService

@RestController
@Tag(name = "Activity")
@RequestMapping("/activity/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class ActivityController(private val activityService: ActivityService) {

    @GetMapping
    @Operation(summary = "Gets all activity for a person’ ")
    fun getPersonActivity(@PathVariable crn: String) = activityService.getPersonActivity(crn)

    @PostMapping
    @Operation(summary = "Activity Log Search’ ")
    fun activitySearch(
        @PathVariable crn: String,
        @RequestBody searchRequest: PersonActivitySearchRequest,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ) = activityService.activitySearch(crn, searchRequest, PageRequest.of(page, size))
}
