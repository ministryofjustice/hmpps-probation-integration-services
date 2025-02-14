package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "AppointmentRemindersAndDelius")
    val TEST_USER = AuditUser(IdGenerator.getAndIncrement(), "TestUser")
}
