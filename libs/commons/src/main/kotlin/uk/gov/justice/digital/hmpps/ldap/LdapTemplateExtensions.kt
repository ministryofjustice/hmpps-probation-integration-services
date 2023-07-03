package uk.gov.justice.digital.hmpps.ldap

import io.opentelemetry.instrumentation.annotations.SpanAttribute
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.ldap.query.SearchScope

@WithSpan
inline fun <reified T> LdapTemplate.findByUsername(@SpanAttribute username: String) = find(
    query()
        .base("ou=Users")
        .searchScope(SearchScope.ONELEVEL)
        .where("cn").`is`(username),
    T::class.java
).singleOrNull()

@WithSpan
fun LdapTemplate.findEmailByUsername(@SpanAttribute username: String) = search(
    query()
        .attributes("mail")
        .base("ou=Users")
        .searchScope(SearchScope.ONELEVEL)
        .where("objectclass").`is`("inetOrgPerson")
        .and("objectclass").`is`("top")
        .and("cn").`is`(username),
    AttributesMapper { it["mail"]?.get()?.toString() }
).singleOrNull()
