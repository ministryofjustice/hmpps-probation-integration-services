package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.ldap.addRole
import uk.gov.justice.digital.hmpps.ldap.removeRole
import uk.gov.justice.digital.hmpps.model.DeliusRole

@Service
class UserService(private val ldapTemplate: LdapTemplate) {

    fun addRole(username: String, role: DeliusRole) = ldapTemplate.addRole(username, role)

    fun removeRole(username: String, role: DeliusRole) = ldapTemplate.removeRole(username, role)
}
