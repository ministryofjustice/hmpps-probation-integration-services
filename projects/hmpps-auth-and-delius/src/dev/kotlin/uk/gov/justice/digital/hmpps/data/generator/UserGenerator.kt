package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "HmppsAuthAndDelius")
    val TEST_USER = AuditUser(IdGenerator.getAndIncrement(), "test.user")
    val INACTIVE_USER = AuditUser(IdGenerator.getAndIncrement(), "test.user.inactive")
}
