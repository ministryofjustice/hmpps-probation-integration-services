package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.User
import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(id(), "AppointmentRemindersAndDelius")
    val TEST_USER = User(id(), "TestUser", StaffGenerator.TEST_STAFF)
}
