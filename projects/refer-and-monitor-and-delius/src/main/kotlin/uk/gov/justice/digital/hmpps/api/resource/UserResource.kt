package uk.gov.justice.digital.hmpps.api.resource

import jakarta.validation.constraints.Size
import org.springframework.data.repository.findByIdOrNull
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
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Validated
@RestController
@RequestMapping("users/{username}")
class UserResource(
    private val managerService: ManagerService,
    private val userService: UserService,
    private val auditUserRepository: AuditUserRepository
) {
    @PreAuthorize("hasAnyRole('CRS_REFERRAL','PROBATION_API__REFER_AND_MONITOR__CASE_DETAIL__RW')")
    @GetMapping("managed-cases")
    fun managedCases(@PathVariable username: String): ManagedCases =
        managerService.findCasesManagedBy(userNameFrom(username))

    @PreAuthorize("hasAnyRole('CRS_REFERRAL','PROBATION_API__REFER_AND_MONITOR__CASE_DETAIL__RW')")
    @RequestMapping("access", method = [RequestMethod.GET, RequestMethod.POST])
    fun userAccessCheck(
        @PathVariable username: String,
        @Size(min = 1, max = 500, message = "Please provide between 1 and 500 crns") @RequestBody crns: List<String>
    ): UserAccess = userService.userAccessFor(userNameFrom(username), crns)

    @PreAuthorize("hasAnyRole('CRS_REFERRAL','PROBATION_API__REFER_AND_MONITOR__CASE_DETAIL__RW')")
    @GetMapping("details")
    fun userDetails(@PathVariable username: String): ResponseEntity<UserDetail> =
        userService.userDetails(userNameFrom(username))?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound()
            .build()

    private fun userNameFrom(idOrUsername: String): String =
        if (idOrUsername.matches("^\\d.*$".toRegex())) {
            auditUserRepository.findByIdOrNull(idOrUsername.toLong())?.username ?: idOrUsername
        } else {
            idOrUsername
        }
}
