package uk.gov.justice.digital.hmpps.service

import io.opentelemetry.instrumentation.annotations.SpanAttribute
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder
import org.springframework.ldap.query.SearchScope
import org.springframework.ldap.support.LdapUtils
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithUser
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsernames

@Service
class LdapService(private val ldapTemplate: LdapTemplate) {
    companion object {
        const val LDAP_MAX_RESULTS_PER_QUERY = 500
    }

    @WithSpan
    fun findEmailForStaff(@SpanAttribute staff: StaffWithUser?) =
        staff?.user?.username?.let { ldapTemplate.findEmailByUsername(it) }

    @WithSpan
    fun findEmailsForStaffIn(@SpanAttribute staff: List<StaffWithUser>) =
        ldapTemplate.findEmailByUsernames(staff.mapNotNull { it.user?.username })

    @WithSpan
    fun findAllUsersWithRole(role: String): List<String> = ldapTemplate.search(
        LdapQueryBuilder.query()
            .attributes("entryDN")
            .searchScope(SearchScope.SUBTREE)
            .where("cn").`is`(role)
            .and("objectclass").`is`("NDRoleAssociation"),
        AttributesMapper {
            LdapUtils.getStringValue(LdapUtils.newLdapName(it["entryDN"]?.get()?.toString()), 3)
        }
    )
}
