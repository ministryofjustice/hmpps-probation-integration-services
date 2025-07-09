package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Staff
import uk.gov.justice.digital.hmpps.entity.User

object StaffGenerator {
    fun generate(user: User? = null) = Staff(
        id = IdGenerator.getAndIncrement(),
        forename = "Forename",
        surname = "Surname",
        user = user
    )
}
