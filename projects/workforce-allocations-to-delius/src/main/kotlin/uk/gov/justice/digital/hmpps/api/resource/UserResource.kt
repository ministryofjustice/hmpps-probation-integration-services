package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
@RequestMapping("/users")
class UserResource(private val userAccessService: UserAccessService) {

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
    @RequestMapping("/limited-access", method = [RequestMethod.GET, RequestMethod.POST])
    fun limitedAccessCheck(
        @Size(min = 1, max = 500, message = "Please provide between 1 and 500 crns") @RequestBody crns: List<String>,
        @RequestParam(required = false) username: String?
    ) = username?.let { userAccessService.userAccessFor(it, crns) } ?: userAccessService.checkLimitedAccessFor(crns)
}
