package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.LdapUser
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.ldap.addRole
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import uk.gov.justice.digital.hmpps.ldap.removeRole
import uk.gov.justice.digital.hmpps.model.DeliusRole
import uk.gov.justice.digital.hmpps.model.UserDetails

@Service
class UserService(private val ldapTemplate: LdapTemplate) {
    fun addRole(username: String, role: DeliusRole) = ldapTemplate.addRole(username, role)

    fun removeRole(username: String, role: DeliusRole) = ldapTemplate.removeRole(username, role)

    fun getUserDetails(username: String) = ldapTemplate.findByUsername<LdapUser>(username)
        ?.let { UserDetails(it.username, it.forename, it.surname, it.email) }
        ?: throw NotFoundException("User", "username", username)
}
