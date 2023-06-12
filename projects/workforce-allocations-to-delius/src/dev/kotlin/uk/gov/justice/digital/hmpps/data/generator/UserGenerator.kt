package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.user.LdapUser
import uk.gov.justice.digital.hmpps.integrations.delius.user.StaffUser
import uk.gov.justice.digital.hmpps.user.AuditUser
import javax.naming.ldap.LdapName

object UserGenerator {
    val AUDIT_USER = generate("HMPPSAllocations")
    val LIMITED_ACCESS_USER = generate("LimitedAccess")
    fun generate(username: String, id: Long = IdGenerator.getAndIncrement()) = AuditUser(id, username)
}

object StaffUserGenerator {
    val DEFAULT = generate("JoeBloggs")

    fun generate(
        username: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = StaffUser(id, username)
}

object LdapUserGenerator {
    val DEFAULT = generate(StaffUserGenerator.DEFAULT.username, "example@example.com")

    fun generate(
        username: String,
        email: String
    ) = LdapUser(LdapName("cn=$username"), username, email)
}
