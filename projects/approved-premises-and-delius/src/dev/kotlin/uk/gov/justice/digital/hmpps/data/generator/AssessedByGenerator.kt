package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.approvedpremises.AssessedBy
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.StaffMember

object AssessedByGenerator {
    fun generate(
        staffMember: StaffMember = StaffMemberGenerator.DEFAULT,
        probationArea: ProbationArea = ProbationArea(
            ProbationAreaGenerator.DEFAULT.code,
            ProbationAreaGenerator.DEFAULT.description
        )
    ) = AssessedBy(
        staffMember = staffMember,
        probationArea = probationArea
    )
}
