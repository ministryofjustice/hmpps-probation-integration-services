package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.User
import java.time.LocalDate

object UserGenerator {
    val AUDIT_USER = User(IdGenerator.getAndIncrement(), "SubjectAccessRequestsAndDelius", "Service")

    val USER1 = User(IdGenerator.getAndIncrement(), "username1", "surname1")
    val USER2 = User(IdGenerator.getAndIncrement(), "username2", "surname2")
    val INACTIVE_USER = User(IdGenerator.getAndIncrement(), "inactive", "inactive", LocalDate.now().minusDays(1))
}
