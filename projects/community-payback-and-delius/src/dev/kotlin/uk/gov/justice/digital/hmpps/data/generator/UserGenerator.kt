package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.User
import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "CommunityPaybackAndDelius")
    val DEFAULT_USER = User(IdGenerator.getAndIncrement(), "DefaultUser")
}
