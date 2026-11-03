package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff

object StaffGenerator {
    val DEFAULT = generate(
        code = "${ProbationAreaGenerator.DEFAULT.code}UTSO",
        forename = "Test",
        surname = "Staff"
    )

    fun generate(
        code: String,
        id: Long = IdGenerator.getAndIncrement(),
        forename: String = "Test",
        forename2: String? = null,
        surname: String = "Staff"
    ) = Staff(
        id = id,
        code = code,
        forename = forename,
        forename2 = forename2,
        surname = surname
    )
}
