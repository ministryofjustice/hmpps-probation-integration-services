package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Staff

object StaffGenerator {
    val DEFAULT_PROBATION_STAFF =
        Staff(
            id = IdGenerator.getAndIncrement(),
            forename = "John",
            middleName = "Bob",
            surname = "Smith",
            ReferenceDataGenerator.MR_TITLE
        )
    val PRISON_OFFENDER_MANAGER_STAFF =
        Staff(
            id = IdGenerator.getAndIncrement(),
            forename = "Jack",
            middleName = "Pom",
            surname = "Harry",
            ReferenceDataGenerator.MR_TITLE
        )
    val PROBATION_STAFF_WITHOUT_USER =
        Staff(id = IdGenerator.getAndIncrement(), forename = "Jane", middleName = "Mary", surname = "Doe")
}
