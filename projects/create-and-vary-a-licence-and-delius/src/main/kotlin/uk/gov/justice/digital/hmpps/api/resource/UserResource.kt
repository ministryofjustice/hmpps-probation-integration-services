package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.ldap.core.LdapTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.DeliusRole
import uk.gov.justice.digital.hmpps.ldap.addRole
import uk.gov.justice.digital.hmpps.ldap.removeRole
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
@RequestMapping("users")
class UserResource(
    private val ldapTemplate: LdapTemplate,
    private val userAccessService: UserAccessService
) {
    @PreAuthorize("hasRole('PROBATION_API__CVL__USER_ROLES__RW')")
    @PutMapping(value = ["/{username}/roles"])
    fun addRole(@PathVariable username: String) =
        ldapTemplate.addRole(username, DeliusRole.LHDCBT002)

    @PreAuthorize("hasRole('PROBATION_API__CVL__USER_ROLES__RW')")
    @DeleteMapping(value = ["/{username}/roles"])
    fun removeRole(@PathVariable username: String) =
        ldapTemplate.removeRole(username, DeliusRole.LHDCBT002)

    @PreAuthorize("hasRole('PROBATION_API__CVL__CASE_DETAIL')")
    @GetMapping("/{username}/access/{crn}")
    fun checkForAccess(@PathVariable username: String, @PathVariable crn: String) =
        userAccessService.caseAccessFor(username, crn)

    @PreAuthorize("hasRole('PROBATION_API__CVL__CASE_DETAIL')")
    @PostMapping("/{username}/access")
    fun checkForAccess(@PathVariable username: String, @RequestBody crns: List<String>) =
        userAccessService.userAccessFor(username, crns)
}
