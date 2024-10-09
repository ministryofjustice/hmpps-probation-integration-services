package uk.gov.justice.digital.hmpps.controller

import org.springframework.ldap.core.LdapTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.ldap.DeliusRole
import uk.gov.justice.digital.hmpps.ldap.addRole
import uk.gov.justice.digital.hmpps.ldap.removeRole

@RestController
class UserController(private val ldapTemplate: LdapTemplate) {
    @PutMapping(value = ["/user/{username}/role"])
    @PreAuthorize("hasRole('PROBATION_API__PATHFINDER__USER_ROLES__RW')")
    fun addRole(@PathVariable username: String) = ldapTemplate.addRole(username, Role.IMSBT001)

    @DeleteMapping(value = ["/user/{username}/role"])
    @PreAuthorize("hasRole('PROBATION_API__PATHFINDER__USER_ROLES__RW')")
    fun removeRole(@PathVariable username: String) = ldapTemplate.removeRole(username, Role.IMSBT001)
}

enum class Role(
    override val description: String,
    override val mappedRole: String
) : DeliusRole {
    IMSBT001("IMS User", "IMSBT001")
}
