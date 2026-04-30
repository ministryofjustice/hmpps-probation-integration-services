package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Staff

object StaffGenerator {
    val DEFAULT_PROBATION_STAFF =
        Staff(id = IdGenerator.getAndIncrement(), forename = "John", middleName = "Bob", surname = "Smith")
    val PROBATION_STAFF_WITHOUT_USER =
        Staff(id = IdGenerator.getAndIncrement(), forename = "Jane", middleName = "Mary", surname = "Doe")
}
