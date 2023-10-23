package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.support.LdapNameBuilder
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.DeliusRole
import javax.naming.directory.Attributes
import javax.naming.directory.BasicAttribute
import javax.naming.directory.BasicAttributes

@Service
class UserService(private val ldapTemplate: LdapTemplate) {
    private val ldapBase = "ou=Users"

    fun addRole(username: String, role: DeliusRole) {
        val roleContext = ldapTemplate.lookupContext(role.context())
            ?: throw NotFoundException("NDeliusRole of ${role.name} not found")
        val attributes: Attributes = BasicAttributes(true).apply {
            put(roleContext.asAttribute("aliasedObjectName"))
            put(role.name.asAttribute("cn"))
            put(listOf("NDRoleAssociation", "Alias", "top").asAttribute("objectclass"))
        }
        val userRole = role.context(username)
        ldapTemplate.rebind(userRole, null, attributes)
    }

    fun removeRole(username: String, role: DeliusRole) =
        ldapTemplate.unbind(role.context(username))

    private fun DeliusRole.context(username: String? = null) =
        LdapNameBuilder.newInstance(ldapBase)
            .add("cn", username ?: "ndRoleCatalogue")
            .add("cn", name)
            .build()

    fun Any.asAttribute(key: String) = BasicAttribute(key, this.toString())
    fun List<Any>.asAttribute(key: String): BasicAttribute =
        BasicAttribute(key).apply { forEach(this::add) }
}
