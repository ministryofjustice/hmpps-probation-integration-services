package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "ManageSupervisionAndDelius")

    val USER = User(id = IdGenerator.getAndIncrement(), forename = "Manage", surname = "Supervision")
}
