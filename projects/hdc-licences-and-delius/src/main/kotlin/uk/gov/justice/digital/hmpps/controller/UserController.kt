package uk.gov.justice.digital.hmpps.controller

import org.springframework.ldap.core.LdapTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.ldap.addRole
import uk.gov.justice.digital.hmpps.ldap.findAttributeByUsername
import uk.gov.justice.digital.hmpps.ldap.getRoles
import uk.gov.justice.digital.hmpps.ldap.removeRole
import uk.gov.justice.digital.hmpps.model.LicencesRole
import uk.gov.justice.digital.hmpps.model.UserDetails
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping(value = ["/users/{username}"])
@PreAuthorize("hasRole('PROBATION_API__HDC__USER_ROLES')")
class UserController(private val ldapTemplate: LdapTemplate) {
    @GetMapping("/details")
    fun getUserDetails(@PathVariable username: String) = UserDetails(
        username = username,
        roles = ldapTemplate.getRoles(username).filter { role -> LicencesRole.entries.any { it.name == role } },
        enabled = ldapTemplate.findAttributeByUsername(username, "endDate")
            .let { it == null || LocalDate.parse(it.substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd")).isAfter(now()) }
    )

    @PutMapping("/roles/{role}")
    fun addRole(@PathVariable username: String, @PathVariable role: LicencesRole) = ldapTemplate.addRole(username, role)

    @DeleteMapping("/roles/{role}")
    fun removeRole(@PathVariable username: String, @PathVariable role: LicencesRole) = ldapTemplate.removeRole(username, role)
}
