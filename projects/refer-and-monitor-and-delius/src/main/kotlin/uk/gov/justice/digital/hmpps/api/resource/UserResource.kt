package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.ManagedCases
import uk.gov.justice.digital.hmpps.api.model.UserAccess
import uk.gov.justice.digital.hmpps.service.ManagerService
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
@RequestMapping("users/{username}")
class UserResource(private val managerService: ManagerService, private val userAccessService: UserAccessService) {
    @PreAuthorize("hasRole('CRS_REFERRAL')")
    @GetMapping("managed-cases")
    fun managedCases(@PathVariable username: String): ManagedCases =
        managerService.findCasesManagedBy(username)

    @PreAuthorize("hasRole('CRS_REFERRAL')")
    @RequestMapping("access", method = [RequestMethod.GET, RequestMethod.POST])
    fun userAccessCheck(@PathVariable username: String, @RequestBody crns: List<String>): UserAccess =
        userAccessService.userAccessFor(username, crns)
}
