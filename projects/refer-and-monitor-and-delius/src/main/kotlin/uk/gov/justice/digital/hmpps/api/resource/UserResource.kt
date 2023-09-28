package uk.gov.justice.digital.hmpps.api.resource

import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.ManagedCases
import uk.gov.justice.digital.hmpps.api.model.UserDetail
import uk.gov.justice.digital.hmpps.service.ManagerService
import uk.gov.justice.digital.hmpps.service.UserAccess
import uk.gov.justice.digital.hmpps.service.UserService

@Validated
@RestController
@RequestMapping("users/{username}")
class UserResource(
    private val managerService: ManagerService,
    private val userService: UserService
) {
    @PreAuthorize("hasRole('CRS_REFERRAL')")
    @GetMapping("managed-cases")
    fun managedCases(@PathVariable username: String): ManagedCases =
        managerService.findCasesManagedBy(username)

    @PreAuthorize("hasRole('CRS_REFERRAL')")
    @RequestMapping("access", method = [RequestMethod.GET, RequestMethod.POST])
    fun userAccessCheck(
        @PathVariable username: String,
        @Size(min = 1, max = 500, message = "Please provide between 1 and 500 crns") @RequestBody crns: List<String>
    ): UserAccess = userService.userAccessFor(username, crns)

    @PreAuthorize("hasRole('CRS_REFERRAL')")
    @GetMapping("details")
    fun userDetails(@PathVariable username: String): ResponseEntity<UserDetail> =
        userService.userDetails(username)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
}
