package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.Staff
import uk.gov.justice.digital.hmpps.entity.User

object UserGenerator {
    fun generate(username: String, staff: Staff? = null) = User(
        id = id(),
        username = username,
        staff = staff,
    )
}
