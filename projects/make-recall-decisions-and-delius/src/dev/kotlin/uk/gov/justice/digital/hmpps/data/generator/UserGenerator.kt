package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.user.access.entity.User
import uk.gov.justice.digital.hmpps.integrations.delius.user.details.entity.UserDetails
import uk.gov.justice.digital.hmpps.integrations.delius.user.details.entity.UserDetailsStaff
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity.StaffUser
import uk.gov.justice.digital.hmpps.user.AuditUser

object UserGenerator {
    val AUDIT_USER = AuditUser(IdGenerator.getAndIncrement(), "MakeRecallDecisionsAndDelius")
    val TEST_USER1 = User(IdGenerator.getAndIncrement(), "Test1")
    val TEST_USER2 = User(IdGenerator.getAndIncrement(), "Test2")
    val WITH_STAFF =
        StaffUser(IdGenerator.getAndIncrement(), "WithStaff", Staff(IdGenerator.getAndIncrement(), "TEST001"))
    val WITHOUT_STAFF = StaffUser(IdGenerator.getAndIncrement(), "WithoutStaff")
    val USER_DETAILS = UserDetails(
        IdGenerator.getAndIncrement(),
        "TestUser",
        "Forename",
        "Middle name",
        "Surname",
        UserDetailsStaff(IdGenerator.getAndIncrement(), "TEST002", null)
    )
}
