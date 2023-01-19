package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.filter.EqualsFilter
import org.springframework.ldap.filter.OrFilter
import org.springframework.ldap.query.LdapQueryBuilder
import org.springframework.ldap.query.SearchScope
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithUser
import uk.gov.justice.digital.hmpps.integrations.delius.user.LdapUser
import uk.gov.justice.digital.hmpps.integrations.delius.user.LdapUserRepository

@Service
class LdapService(
    private val ldapUserRepository: LdapUserRepository,
    private val ldapTemplate: LdapTemplate,
) {
    companion object {
        const val LDAP_MAX_RESULTS_PER_QUERY = 500
    }

    fun findEmailForStaff(staff: StaffWithUser?) = staff?.user?.username?.let { ldapUserRepository.findByUsername(it)?.email }

    fun findEmailsForStaffIn(staff: List<StaffWithUser>): Map<String, String?> {
        return staff.mapNotNull { it.user?.username }
            .distinct()
            .chunked(LDAP_MAX_RESULTS_PER_QUERY)
            .flatMap {
                val filter = it.map { username -> EqualsFilter("cn", username) }.fold(OrFilter()) { a, b -> a.or(b) }
                val query = LdapQueryBuilder.query()
                    .base("ou=Users")
                    .searchScope(SearchScope.ONELEVEL)
                    .filter(filter)
                ldapTemplate.find(query, LdapUser::class.java)
            }
            .associate { it.username to it.email }
    }
}
