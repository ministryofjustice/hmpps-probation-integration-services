package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff

object StaffGenerator {
    val DEFAULT = generate("${ProbationAreaGenerator.DEFAULT.code}UTSO")
    fun generate(
        code: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(id, code)
}
