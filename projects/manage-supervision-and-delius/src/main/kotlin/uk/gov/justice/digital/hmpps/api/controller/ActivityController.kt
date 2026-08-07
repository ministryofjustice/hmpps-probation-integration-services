package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpServerErrorException
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
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
    ) = handleServiceUnavailable { activityService.activitySearch(crn, "2", searchRequest, PageRequest.of(page, size)) }

    @GetMapping("/preload")
    @Operation(summary = "Preload semantic search data for a person")
    fun preloadForCrn(
        @PathVariable crn: String
    ) = handleServiceUnavailable { activityService.preloadForCrn(crn) }

    // May get a 503 back from search to indicate indexing is still in progress, so pass that back to the client
    private fun <T> handleServiceUnavailable(fn: () -> T) = try {
        ResponseEntity.ok(fn())
    } catch (e: HttpServerErrorException.ServiceUnavailable) {
        ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .apply { e.responseHeaders?.getFirst(HttpHeaders.RETRY_AFTER)?.let { header(HttpHeaders.RETRY_AFTER, it) } }
            .body(ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), e.message))
    }
}
