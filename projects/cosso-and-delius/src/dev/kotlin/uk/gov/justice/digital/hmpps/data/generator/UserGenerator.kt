package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.User
import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER: AuditUser = AuditUser(IdGenerator.getAndIncrement(), "CossoAndDelius")
    val DEFAULT_PROBATION_USER: User = User(
        IdGenerator.getAndIncrement(),
        StaffGenerator.DEFAULT_PROBATION_STAFF,
        "J0nSm17h"
    )
    val POM_USER: User = User(
        IdGenerator.getAndIncrement(),
        StaffGenerator.PRISON_OFFENDER_MANAGER_STAFF,
        "JackHarry"
    )
}
