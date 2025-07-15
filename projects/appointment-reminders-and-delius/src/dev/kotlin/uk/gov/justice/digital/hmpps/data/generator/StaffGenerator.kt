package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.Staff

object StaffGenerator {
    val TEST_STAFF = Staff(
        id = id(),
        code = "TEST01",
        forename = "Test",
        surname = "Staff",
        user = null
    )
}
