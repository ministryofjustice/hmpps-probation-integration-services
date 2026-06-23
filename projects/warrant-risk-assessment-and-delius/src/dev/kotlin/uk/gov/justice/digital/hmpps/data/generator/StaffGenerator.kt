package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Staff

object StaffGenerator {
    val DEFAULT_STAFF = Staff(
        id = IdGenerator.getAndIncrement(),
        forename = "Billy",
        middleName = "The",
        surname = "Kid",
    )
}