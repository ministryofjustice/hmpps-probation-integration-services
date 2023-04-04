package uk.gov.justice.digital.hmpps.integrations.ldap

import io.opentelemetry.instrumentation.annotations.SpanAttribute
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder
import org.springframework.ldap.query.SearchScope
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.ldap.entity.LdapUser

@Service
class LdapService(private val ldapTemplate: LdapTemplate) {
    @WithSpan
    fun findEmailByUsername(@SpanAttribute username: String) = ldapTemplate.find(
        LdapQueryBuilder.query()
            .attributes("mail")
            .base("ou=Users")
            .searchScope(SearchScope.ONELEVEL)
            .where("cn").`is`(username),
        LdapUser::class.java
    ).singleOrNull()?.email
}
