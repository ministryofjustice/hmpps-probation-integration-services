package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.approvedpremises.StaffMember

object StaffMemberGenerator {
    val DEFAULT = generate()

    fun generate(
        staffCode: String = "N54A001",
        staffIdentifier: Long = 1501234567,
        forenames: String = "John",
        surname: String = "Smith",
        username: String = "JohnSmithNPS"
    ) = StaffMember(
        staffCode = staffCode,
        staffIdentifier = staffIdentifier,
        forenames = forenames,
        surname = surname,
        username = username
    )
}
