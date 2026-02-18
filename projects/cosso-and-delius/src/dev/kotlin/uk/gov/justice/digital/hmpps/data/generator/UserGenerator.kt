package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.User
import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER: AuditUser = AuditUser(IdGenerator.getAndIncrement(), "CossoAndDelius")
    val DEFAULT_PROBATION_USER: User = User(
        IdGenerator.getAndIncrement(),
        StaffGenerator.DEFAULT_PROBATION_STAFF.id,
        "J0nSm17h"
    ) // Use a placeholder staffId
}
