package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.User

object UserGenerator {
    fun generate(username: String, staff: Staff? = null) = User(
        id = id(),
        username = username,
        staff = staff,
    )
}
