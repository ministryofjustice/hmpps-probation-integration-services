package uk.gov.justice.digital.hmpps.ldap

import io.opentelemetry.instrumentation.annotations.SpanAttribute
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQuery
import org.springframework.ldap.query.LdapQueryBuilder
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.ldap.query.SearchScope
import org.springframework.ldap.support.LdapNameBuilder
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import javax.naming.directory.Attributes
import javax.naming.directory.BasicAttribute
import javax.naming.directory.BasicAttributes

private const val ldapBase = "ou=Users"
fun LdapQueryBuilder.byUsername(username: String): LdapQuery =
    base(ldapBase).searchScope(SearchScope.ONELEVEL).where("cn").`is`(username)

@WithSpan
inline fun <reified T> LdapTemplate.findByUsername(@SpanAttribute username: String) =
    find(query().byUsername(username), T::class.java).singleOrNull()

@WithSpan
fun LdapTemplate.findEmailByUsername(@SpanAttribute username: String) = search(
    query()
        .attributes("mail")
        .base(ldapBase)
        .searchScope(SearchScope.ONELEVEL)
        .where("objectclass").`is`("inetOrgPerson")
        .and("objectclass").`is`("top")
        .and("cn").`is`(username),
    AttributesMapper { it["mail"]?.get()?.toString() }
).singleOrNull()

fun LdapTemplate.addRole(username: String, role: DeliusRole) {
    val roleContext = lookupContext(role.context())
        ?: throw NotFoundException("NDeliusRole of ${role.name} not found")
    val attributes: Attributes = BasicAttributes(true).apply {
        put(roleContext.asAttribute("aliasedObjectName"))
        put(role.name.asAttribute("cn"))
        put(listOf("NDRoleAssociation", "Alias", "top").asAttribute("objectclass"))
    }
    val userRole = role.context(username)
    rebind(userRole, null, attributes)
}

fun LdapTemplate.removeRole(username: String, role: DeliusRole) =
    unbind(role.context(username))

private fun DeliusRole.context(username: String? = null) =
    LdapNameBuilder.newInstance(ldapBase)
        .add("cn", username ?: "ndRoleCatalogue")
        .add("cn", name)
        .build()

fun Any.asAttribute(key: String) = BasicAttribute(key, this.toString())
fun List<Any>.asAttribute(key: String): BasicAttribute =
    BasicAttribute(key).apply { forEach(this::add) }
