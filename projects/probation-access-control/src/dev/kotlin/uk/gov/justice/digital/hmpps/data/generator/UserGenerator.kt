package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "ProbationAccessControl")

    val DEFAULT = LimitedAccessUser("officer", IdGenerator.getAndIncrement())
    val RESTRICTED = LimitedAccessUser("restricted", IdGenerator.getAndIncrement())
}
