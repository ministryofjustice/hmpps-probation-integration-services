package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.SearchService

@RestController
@PreAuthorize("hasRole('PROBATION_API__HDC__CASE_DETAIL')")
class OffenderSearch(private val searchService: SearchService) {
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorised, requires a valid Oauth2 token",
                content = [Content(examples = [])]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden, requires an authorisation with role PROBATION_API__HDC__CASE_DETAIL",
                content = [Content(examples = [])]
            ),
        ],
    )
    @PostMapping("/nomsNumbers")
    @Operation(
        description = "Match prisoners by a list of prisoner noms numbers",
        summary = "Requires ROLE_PROBATION__SEARCH_PERSON role"
    )
    fun findByNomsNumbers(
        @Parameter(required = true, name = "nomsList") @RequestBody nomsList: List<String>,
    ) = searchService.findByListOfNoms(nomsList)
}