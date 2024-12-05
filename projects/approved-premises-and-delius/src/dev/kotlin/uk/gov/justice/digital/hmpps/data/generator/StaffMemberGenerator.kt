package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.approvedpremises.StaffMember

object StaffMemberGenerator {
    val DEFAULT = generate()

    fun generate(
        staffCode: String = "N54A001",
        forenames: String = "John",
        surname: String = "Smith",
        username: String? = null
    ) = StaffMember(
        staffCode = staffCode,
        forenames = forenames,
        surname = surname,
        username = username
    )
}
