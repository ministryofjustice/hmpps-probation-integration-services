package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.UserAccess
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
@RequestMapping("users")
class UserResource(private val uas: UserAccessService) {
    @PreAuthorize("hasRole('ROLE__PROBATION_API__ARNS__USER_ACCESS')")
    @RequestMapping("access", method = [RequestMethod.GET, RequestMethod.POST])
    fun userAccessCheck(
        @RequestParam(required = false) username: String?,
        @Size(min = 1, max = 500, message = "Please provide between 1 and 500 crns") @RequestBody crns: List<String>
    ): UserAccess = username?.let { uas.userAccessFor(it, crns) } ?: uas.checkLimitedAccessFor(crns)
}
