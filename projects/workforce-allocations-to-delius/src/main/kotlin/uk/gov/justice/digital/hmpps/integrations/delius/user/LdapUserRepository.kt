package uk.gov.justice.digital.hmpps.integrations.delius.user

import org.springframework.data.ldap.repository.LdapRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff

interface LdapUserRepository : LdapRepository<LdapUser> {
    fun findByUsername(username: String): LdapUser?
}

fun LdapUserRepository.findEmailForStaff(staff: Staff?) = staff?.user?.username?.let { findByUsername(it)?.email }
