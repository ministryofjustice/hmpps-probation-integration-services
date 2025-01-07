package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.constraints.Size
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.service.UserAccessService
import uk.gov.justice.digital.hmpps.service.UserService

@RestController
class UserResource(
    private val userAccessService: UserAccessService,
    private val userService: UserService,
) {

    @PreAuthorize("hasRole('PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    @Operation(
        summary = """Data access restriction information for the combination of the Delius
            user and the supplied list of CRNs""",
        description = """Probation case records in Delius may have access restrictions
            defined for particular Delius users. This restrictions may be in place to stop
            identified users accessing information on specific people or they may restrict
            access to all users other than those named. The restrictions may be in place
            for a number of reasons related to the case supervision and clients should use
            the restriction information to mask data access from any identified user that
            has a restriction in place
        """
    )
    @RequestMapping("/users/limited-access", method = [RequestMethod.GET, RequestMethod.POST])
    fun limitedAccessCheck(
        @Size(min = 1, max = 500, message = "Please provide between 1 and 500 crns") @RequestBody crns: List<String>,
        @RequestParam(required = false) username: String?
    ) = username?.let { userAccessService.userAccessFor(it, crns) } ?: userAccessService.checkLimitedAccessFor(crns)

    @GetMapping("/users")
    @Operation(summary = "Returns all users with the Delius `MAABT001` role")
    fun allUsers() = ResponseEntity.ok()
        .contentType(APPLICATION_JSON)
        .body(StreamingResponseBody { userService.writeAllUsersWithRole(it) })

    @GetMapping("/person/{crn}/limited-access/all")
    @Operation(summary = "Returns all limited access information (restrictions and exclusions) for a Delius CRN")
    fun allAccessLimitationsForCrn(@PathVariable crn: String) = userService.getAllAccessLimitations(crn)

    @PostMapping("/person/{crn}/limited-access")
    @Operation(summary = "Returns limited access information (restrictions and exclusions) for a Delius CRN, given a list of staff codes")
    fun allAccessLimitationsForCrnAndUserList(
        @PathVariable crn: String,
        @Size(min = 0, max = 500, message = "Please provide up to 500 staff codes to filter by")
        @RequestBody(required = false) staffCodes: List<String>? = null,
    ) = userService.getAllAccessLimitations(crn, staffCodes)
}
