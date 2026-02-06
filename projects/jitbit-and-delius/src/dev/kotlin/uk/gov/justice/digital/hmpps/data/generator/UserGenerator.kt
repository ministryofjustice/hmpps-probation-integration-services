package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.User
import uk.gov.justice.digital.hmpps.user.AuditUser
import java.time.LocalDate

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "JitbitAndDelius")
    val LIMITED_ACCESS_USER = AuditUser(IdGenerator.getAndIncrement(), "LimitedAccess")
    val NORMAL_USER = User(IdGenerator.getAndIncrement(), "test.user")
    val MULTI_USER_1 = User(IdGenerator.getAndIncrement(), "test.user2")
    val MULTI_USER_2 = User(IdGenerator.getAndIncrement(), "test.user3")
    val EXPIRED_USER = User(IdGenerator.getAndIncrement(), "expired.user", LocalDate.now().minusDays(7))
}
