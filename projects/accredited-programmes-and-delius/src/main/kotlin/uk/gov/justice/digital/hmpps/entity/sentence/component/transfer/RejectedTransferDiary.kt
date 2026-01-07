package uk.gov.justice.digital.hmpps.entity.sentence.component.transfer

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement
import uk.gov.justice.digital.hmpps.entity.sentence.component.SentenceComponent
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.ZonedDateTime

@Entity
class RejectedTransferDiary(
    @Id
    @SequenceGenerator(
        name = "rejected_transfer_diary_id_generator",
        sequenceName = "rejected_transfer_diary_id_seq",
        allocationSize = 1
    )
    @GeneratedId(generator = "rejected_transfer_diary_id_generator")
    @Column(name = "rejected_transfer_diary_id", nullable = false)
    val id: Long = 0,

    @Column(name = "offender_id", nullable = false)
    val personId: Long,

    @Column(name = "transfer_request_date", nullable = false)
    val requestDate: ZonedDateTime,

    @Column(name = "transfer_status_date")
    val statusDate: ZonedDateTime? = null,

    @Column
    val eventId: Long? = null,

    component: SentenceComponent,
    transfer: Transfer,

    @Column(name = "lic_condition_id")
    val licenceConditionId: Long? = (component as? LicenceCondition)?.id,

    @Column(name = "lic_condition_transfer_id")
    val licenceConditionTransferId: Long? = (transfer as? LicenceConditionTransfer)?.id,

    @Column(name = "rqmnt_id")
    val requirementId: Long? = (component as? Requirement)?.id,

    @Column(name = "rqmnt_transfer_id")
    val requirementTransferId: Long? = (transfer as? RequirementTransfer)?.id,

    @Column(nullable = false)
    val rejectionReasonId: Long,

    @Column(nullable = false)
    val targetProviderId: Long,

    @Column(nullable = false)
    val targetTeamId: Long,

    @Column(nullable = false)
    val targetStaffId: Long,

    @Column(nullable = false)
    val originProviderId: Long,

    @Column(nullable = false)
    val originTeamId: Long,

    @Column(nullable = false)
    val originStaffId: Long,

    @Column
    val masterTransferId: Long? = null
)
