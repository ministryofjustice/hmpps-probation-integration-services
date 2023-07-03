package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = generate("ReferAndMonitorAndDelius")
    val LIMITED_ACCESS_USER = generate("LimitedAccess")

    fun generate(username: String, id: Long = IdGenerator.getAndIncrement()) = AuditUser(id, username)
}
