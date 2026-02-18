package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Staff
import uk.gov.justice.digital.hmpps.entity.User

object StaffGenerator {
    val DEFAULT_PROBATION_STAFF = Staff(id = IdGenerator.getAndIncrement(), forename = "John", middleName = "Bob", surname = "Smith")
}