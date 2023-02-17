package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.user.LdapUser
import uk.gov.justice.digital.hmpps.integrations.delius.user.StaffUser
import uk.gov.justice.digital.hmpps.user.User
import javax.naming.ldap.LdapName

object UserGenerator {
    val APPLICATION_USER = User(IdGenerator.getAndIncrement(), "HMPPSAllocations")
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
    ) = LdapUser(LdapName("cn=$username"), username, username, email)
}
