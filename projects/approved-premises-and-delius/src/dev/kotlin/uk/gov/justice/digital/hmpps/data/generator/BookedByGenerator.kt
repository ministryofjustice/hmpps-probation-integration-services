package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.approvedpremises.BookedBy
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.StaffMember

object BookedByGenerator {
    fun generate(staffMember: StaffMember = StaffMemberGenerator.DEFAULT) =
        BookedBy(
            staffMember = staffMember
        )
}
