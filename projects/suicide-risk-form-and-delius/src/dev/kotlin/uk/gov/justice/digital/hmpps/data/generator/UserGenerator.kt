package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.User
import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "SuicideRiskFormAndDelius")
    val LIMITED_ACCESS_USER = AuditUser(IdGenerator.getAndIncrement(), "LimitedAccess")
    val NON_LAO_USER = AuditUser(IdGenerator.getAndIncrement(), "NonLao")
    val DEFAULT = User(
        IdGenerator.getAndIncrement(),
        "officer",
        StaffGenerator.DEFAULT,
    )

    val OFFICER_2 = User(
        IdGenerator.getAndIncrement(),
        "officer2",
        StaffGenerator.OFFICER_2,
    )
}

object StaffGenerator {
    val DEFAULT = Staff(
        id = IdGenerator.getAndIncrement(),
        code = "N00A001",
        firstName = "Probation",
        middleName = "PO",
        surname = "Officer",
        user = null
    )

    val OFFICER_2 = Staff(
        id = IdGenerator.getAndIncrement(),
        code = "N00A002",
        firstName = "Probation",
        middleName = "PO",
        surname = "Officer 2",
        user = null
    )
}
