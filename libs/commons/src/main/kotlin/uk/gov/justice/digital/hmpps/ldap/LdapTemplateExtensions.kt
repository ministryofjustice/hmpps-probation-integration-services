package uk.gov.justice.digital.hmpps.ldap

import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.annotations.SpanAttribute
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.ldap.NameAlreadyBoundException
import org.springframework.ldap.NameNotFoundException
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQuery
import org.springframework.ldap.query.LdapQueryBuilder
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.ldap.query.SearchScope
import org.springframework.ldap.support.LdapNameBuilder
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import javax.naming.Name
import javax.naming.directory.Attributes
import javax.naming.directory.BasicAttribute
import javax.naming.directory.BasicAttributes

fun LdapQueryBuilder.byUsername(username: String): LdapQuery =
    searchScope(SearchScope.ONELEVEL).where("cn").`is`(username)

@WithSpan
inline fun <reified T> LdapTemplate.findByUsername(@SpanAttribute username: String) =
    find(query().byUsername(username), T::class.java).singleOrNull()

@WithSpan
fun LdapTemplate.findEmailByUsername(@SpanAttribute username: String) = findAttributeByUsername(username, "mail")

@WithSpan
fun LdapTemplate.findAttributeByUsername(@SpanAttribute username: String, @SpanAttribute attribute: String) = try {
    search(query()
        .attributes(attribute)
        .searchScope(SearchScope.ONELEVEL)
        .where("objectclass").`is`("inetOrgPerson")
        .and("objectclass").`is`("top")
        .and("cn").`is`(username),
        AttributesMapper { it[attribute]?.get()?.toString() }
    ).singleOrNull()
} catch (_: NameNotFoundException) {
    throw NotFoundException("User", "username", username)
}

@WithSpan
fun LdapTemplate.getRoles(@SpanAttribute username: String) = try {
    search(query()
        .attributes("cn")
        .base(LdapNameBuilder.newInstance().add("cn", username).build())
        .searchScope(SearchScope.ONELEVEL)
        .where("objectclass").`is`("NDRole")
        .or("objectclass").`is`("NDRoleAssociation"),
        AttributesMapper { it["cn"]?.get()?.toString() }
    ).filterNotNull()
} catch (_: NameNotFoundException) {
    throw NotFoundException("User", "username", username)
}

@WithSpan
fun LdapTemplate.addRole(@SpanAttribute username: String, @SpanAttribute role: DeliusRole) {
    val roleContext = lookupContext(role.context()) ?: throw NotFoundException("Role", "name", role.name)
    val attributes: Attributes = BasicAttributes(true).apply {
        put(roleContext.nameInNamespace.asAttribute("aliasedObjectName"))
        put(role.name.asAttribute("cn"))
        put(listOf("NDRoleAssociation", "alias", "top").asAttribute("objectclass"))
    }
    val userRole = role.context(username)
    if (!exists(userRole)) {
        try {
            rebind(userRole, null, attributes)
        } catch (_: NameNotFoundException) {
            throw NotFoundException("User", "username", username)
        } catch (_: NameAlreadyBoundException) {
            // role already assigned to user
        }
    }
}

@WithSpan
fun LdapTemplate.removeRole(@SpanAttribute username: String, @SpanAttribute role: DeliusRole) {
    val userRole = role.context(username)
    if (exists(userRole)) {
        try {
            unbind(role.context(username))
        } catch (_: NameNotFoundException) {
            throw NotFoundException("User", "username", username)
        }
    }
}

@WithSpan
fun LdapTemplate.exists(name: Name) = try {
    lookup(name)
    true
} catch (_: NameNotFoundException) {
    false
}.also {
    Span.current().setAttribute("exists", it)
}

private fun DeliusRole.context(username: String? = null) =
    LdapNameBuilder.newInstance()
        .add("cn", username ?: "ndRoleCatalogue")
        .add("cn", name)
        .build()

private fun Any.asAttribute(key: String) = BasicAttribute(key, this.toString())
private fun List<Any>.asAttribute(key: String): BasicAttribute =
    BasicAttribute(key).apply { forEach(this::add) }
