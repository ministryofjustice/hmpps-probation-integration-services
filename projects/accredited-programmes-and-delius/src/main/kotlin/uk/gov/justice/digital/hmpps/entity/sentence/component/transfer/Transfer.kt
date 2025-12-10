package uk.gov.justice.digital.hmpps.entity.sentence.component.transfer

import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.component.SentenceComponent
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import java.time.ZonedDateTime

interface Transfer {
    val id: Long
    var status: ReferenceData
    var decision: ReferenceData?
    val allocationReason: ReferenceData?
    var rejectionReason: ReferenceData?
    var statusDate: ZonedDateTime?
    var notes: String?
    val requestDate: ZonedDateTime
    val receivingTeam: Team
    val receivingStaff: Staff
    val originTeam: Team
    val originStaff: Staff
    val masterTransferId: Long?
    val component: SentenceComponent
}