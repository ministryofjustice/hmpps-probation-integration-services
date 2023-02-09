package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.common.entity.staff.Staff

object StaffGenerator {
    val DEFAULT = generate()
    fun generate(
        code: String = "STAFF1",
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(id, code)
}
