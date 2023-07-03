package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.approvedpremises.StaffMember
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.SubmittedBy
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea

object SubmittedByGenerator {
    fun generate(
        staffMember: StaffMember = StaffMemberGenerator.DEFAULT,
        probationArea: ProbationArea = ProbationAreaGenerator.DEFAULT
    ) = SubmittedBy(
        staffMember = staffMember,
        probationArea = probationArea
    )
}
