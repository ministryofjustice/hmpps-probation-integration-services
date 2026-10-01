package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.staff.User
import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "EmdiAndDelius")

    val DEFAULT_USER = generate(
        distinguishedName = "CN=john.smith,OU=staff,DC=justice,DC=gov,DC=uk",
        staffId = 1001L
    )

    val LONDON_USER = generate(
        distinguishedName = "CN=jane.doe,OU=staff,DC=justice,DC=gov,DC=uk",
        staffId = 1002L
    )

    val MULTI_TEAM_USER = generate(
        distinguishedName = "CN=bob.wilson,OU=staff,DC=justice,DC=gov,DC=uk",
        staffId = 1003L
    )

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        distinguishedName: String,
        staffId: Long? = null
    ) = User(id, staffId, distinguishedName)
}
