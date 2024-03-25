package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.approvedpremises.AssessedBy
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.StaffMember
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea

object AssessedByGenerator {
    fun generate(
        staffMember: StaffMember = StaffMemberGenerator.DEFAULT,
        probationArea: ProbationArea = ProbationAreaGenerator.DEFAULT
    ) = AssessedBy(
        staffMember = staffMember,
        probationArea = probationArea
    )
}
