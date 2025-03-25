package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.model.CrnRequest
import uk.gov.justice.digital.hmpps.service.UserAccess
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
@RequestMapping("probation-cases")
@PreAuthorize("hasRole('PROBATION_API__HMPPS_API__CASE_DETAIL')")
class LaoAccessController(private val uas: UserAccessService) {
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "OK",
            content = [
                Content(
                    schema = Schema(implementation = UserAccess::class),
                    examples = [
                        ExampleObject("""{ "access": [ { "crn": "A123456", "userExcluded": false, "userRestricted": false } ] }""")
                    ]
                )
            ]
        ),
    )
    @RequestMapping("access", method = [RequestMethod.GET, RequestMethod.POST])
    fun caseAccess(
        @RequestParam(required = false) username: String?,
        @Valid @RequestBody request: CrnRequest
    ): UserAccess = username?.let { uas.userAccessFor(it, request.crns) } ?: uas.checkLimitedAccessFor(request.crns)
}