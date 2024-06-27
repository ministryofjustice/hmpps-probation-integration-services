package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.StaffMember
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.SubmittedBy

object SubmittedByGenerator {
    fun generate(
        staffMember: StaffMember = StaffMemberGenerator.DEFAULT,
        probationArea: ProbationArea = ProbationArea(
            ProbationAreaGenerator.DEFAULT.code,
            ProbationAreaGenerator.DEFAULT.description
        )
    ) = SubmittedBy(
        staffMember = staffMember,
        probationArea = probationArea
    )
}
