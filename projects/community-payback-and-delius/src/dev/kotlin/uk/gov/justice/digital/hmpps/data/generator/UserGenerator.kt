package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.staff.User
import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "CommunityPaybackAndDelius")
    val DEFAULT_USER = User(IdGenerator.getAndIncrement(), "DefaultUser", StaffGenerator.DEFAULT_STAFF)
}
