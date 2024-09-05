package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "UnpaidWorkAndDelius")
    val LIMITED_ACCESS_USER = AuditUser(IdGenerator.getAndIncrement(), "LimitedAccess")
}
