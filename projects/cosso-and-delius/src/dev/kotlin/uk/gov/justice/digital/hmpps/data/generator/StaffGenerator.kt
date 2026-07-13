package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.Staff

object StaffGenerator {
    val DEFAULT_PROBATION_STAFF =
        Staff(
            id = id(),
            forename = "John",
            middleName = "Bob",
            surname = "Smith",
            title = ReferenceDataGenerator.MR_TITLE,
            user = UserGenerator.DEFAULT_PROBATION_USER,
        )
    val PRISON_OFFENDER_MANAGER_STAFF =
        Staff(
            id = id(),
            forename = "Jack",
            middleName = "Pom",
            surname = "Harry",
            title = ReferenceDataGenerator.MR_TITLE,
            user = UserGenerator.POM_USER,
        )
    val PROBATION_STAFF_WITHOUT_USER =
        Staff(id = id(), forename = "Jane", middleName = "Mary", surname = "Doe", user = null)
}
