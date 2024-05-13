package uk.gov.justice.digital.hmpps.service

import io.opentelemetry.instrumentation.annotations.SpanAttribute
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.filter.EqualsFilter
import org.springframework.ldap.filter.OrFilter
import org.springframework.ldap.query.LdapQueryBuilder
import org.springframework.ldap.query.SearchScope
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithUser
import uk.gov.justice.digital.hmpps.integrations.delius.user.LdapUser
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername

@Service
class LdapService(private val ldapTemplate: LdapTemplate) {
    companion object {
        const val LDAP_MAX_RESULTS_PER_QUERY = 500
    }

    @WithSpan
    fun findEmailForStaff(@SpanAttribute staff: StaffWithUser?) =
        staff?.user?.username?.let { ldapTemplate.findEmailByUsername(it) }

    @WithSpan
    fun findEmailsForStaffIn(@SpanAttribute staff: List<StaffWithUser>) = staff.mapNotNull { it.user?.username }
        .distinct()
        .chunked(LDAP_MAX_RESULTS_PER_QUERY)
        .flatMap {
            val filter = it.map { username -> EqualsFilter("cn", username) }.fold(OrFilter()) { a, b -> a.or(b) }
            val query = LdapQueryBuilder.query()
                .attributes("mail")
                .searchScope(SearchScope.ONELEVEL)
                .filter(filter)
            ldapTemplate.find(query, LdapUser::class.java)
        }
        .associate { it.username to it.email }
}
