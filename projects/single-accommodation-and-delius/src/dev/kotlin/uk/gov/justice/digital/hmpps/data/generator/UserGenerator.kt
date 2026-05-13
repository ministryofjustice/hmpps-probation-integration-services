package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.StaffUser
import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "SingleAccommodationAndDelius")
    val DEFAULT = StaffUser(
        id = IdGenerator.getAndIncrement(),
        username = "officer",
        staff = StaffGenerator.DEFAULT,
    )
    val OTHER = StaffUser(
        id = IdGenerator.getAndIncrement(),
        username = "bothteamsofficer",
        staff = StaffGenerator.BOTH_TEAMS_STAFF,
    )
}
