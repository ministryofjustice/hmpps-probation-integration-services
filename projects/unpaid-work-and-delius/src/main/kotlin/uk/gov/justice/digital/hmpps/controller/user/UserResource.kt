package uk.gov.justice.digital.hmpps.controller.user

import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseAccess
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Validated
@RestController
@RequestMapping("users/{username}")
class UserResource(
    private val userService: UserService,
    private val auditUserRepository: AuditUserRepository
) {

    @PreAuthorize("hasRole('PROBATION_API__UPW__CASE_DETAIL')")
    @GetMapping(value = ["/access/{crn}"])
    fun userAccessCheck(
        @PathVariable username: String,
        @PathVariable crn: String
    ): CaseAccess = userService.userAccessFor(userNameFrom(username), crn)

    private fun userNameFrom(idOrUsername: String): String =
        if (idOrUsername.matches("^\\d.*$".toRegex())) {
            auditUserRepository.findByIdOrNull(idOrUsername.toLong())?.username ?: idOrUsername
        } else {
            idOrUsername
        }
}
