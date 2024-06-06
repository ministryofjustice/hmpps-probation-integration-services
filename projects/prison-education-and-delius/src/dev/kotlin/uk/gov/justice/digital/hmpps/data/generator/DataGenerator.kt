package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.set

object DataGenerator {
    val PERSON = Person(id = IdGenerator.getAndIncrement(), prisonerId = "A0000AA")

    val STAFF = Staff(id = IdGenerator.getAndIncrement(), forename = "Test", surname = "Staff", user = null)
    val STAFF_WITH_USER = Staff(id = IdGenerator.getAndIncrement(), forename = "Test", surname = "User", user = null)
        .set(Staff::user) { UserDetails(id = IdGenerator.getAndIncrement(), username = "TestUser", staff = it) }

    val COMMUNITY_MANAGER = CommunityManager(id = IdGenerator.getAndIncrement(), person = PERSON, staff = STAFF)
    val COMMUNITY_MANAGER_WITH_USER =
        CommunityManager(id = IdGenerator.getAndIncrement(), person = PERSON, staff = STAFF_WITH_USER)

    val MAIN_ADDRESS_TYPE = ReferenceData(id = IdGenerator.getAndIncrement(), code = "M", description = "Main")
    val MAIN_ADDRESS =
        Address(
            id = IdGenerator.getAndIncrement(),
            person = PERSON,
            status = MAIN_ADDRESS_TYPE,
            buildingName = "Building Name",
            addressNumber = "123",
            streetName = "Street Name",
            district = "District",
            townCity = "Town City",
            county = "County",
            postcode = "AA1 1AA",
            noFixedAbode = false,
            endDate = null,
            softDeleted = false
        )
}
