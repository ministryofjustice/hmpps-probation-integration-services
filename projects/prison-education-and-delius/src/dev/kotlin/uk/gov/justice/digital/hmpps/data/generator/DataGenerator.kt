package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.CommunityManager
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.Staff
import uk.gov.justice.digital.hmpps.entity.UserDetails
import uk.gov.justice.digital.hmpps.set

object DataGenerator {
    val PERSON = Person(id = IdGenerator.getAndIncrement(), prisonerId = "A0000AA")

    val STAFF = Staff(id = IdGenerator.getAndIncrement(), forename = "Test", surname = "Staff", user = null)
    val STAFF_WITH_USER = Staff(id = IdGenerator.getAndIncrement(), forename = "Test", surname = "User", user = null)
        .set(Staff::user) { UserDetails(id = IdGenerator.getAndIncrement(), username = "TestUser", staff = it) }

    val COMMUNITY_MANAGER = CommunityManager(id = IdGenerator.getAndIncrement(), personId = PERSON.id, staff = STAFF)
    val COMMUNITY_MANAGER_WITH_USER =
        CommunityManager(id = IdGenerator.getAndIncrement(), personId = PERSON.id, staff = STAFF_WITH_USER)
}
