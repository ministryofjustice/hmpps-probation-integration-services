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
    @Operation(
        summary = "Gets all activity for a person",
        description = """Returns all contacts in descending date order
            (most recent first) under 'activities'."""
    )
    fun getPersonActivity(@PathVariable crn: String) = activityService.getPersonActivity(crn)

    @PostMapping
    @Operation(summary = "Activity log search for a person")
    fun activitySearch(
        @PathVariable crn: String,
        @RequestBody searchRequest: PersonActivitySearchRequest,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ) = activityService.activitySearch(crn, "1", searchRequest, PageRequest.of(page, size))

    @PostMapping("/v2")
    @Operation(summary = "Activity log search for a person (v2)")
    fun activitySearchV2(
        @PathVariable crn: String,
        @RequestBody searchRequest: PersonActivitySearchRequest,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ) = activityService.activitySearch(crn, "2", searchRequest, PageRequest.of(page, size))

    @GetMapping("/preload")
    @Operation(summary = "Preload semantic search data for a person")
    fun preloadForCrn(
        @PathVariable crn: String
    ) = activityService.preloadForCrn(crn)
}
