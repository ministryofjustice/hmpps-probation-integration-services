package uk.gov.justice.digital.hmpps.integrations.delius.user

import org.springframework.data.ldap.repository.LdapRepository

interface LdapUserRepository : LdapRepository<LdapUser> {
    fun findByUsername(username: String): LdapUser?
}
