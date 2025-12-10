package uk.gov.justice.digital.hmpps.entity.sentence.component.transfer

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.component.SentenceComponent
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import java.time.ZonedDateTime

@Entity
@SQLRestriction("soft_deleted = 0")
@Table(name = "lic_condition_transfer")
@EntityListeners(AuditingEntityListener::class)
class LicenceConditionTransfer(
    @Id
    @Column(name = "lic_condition_transfer_id", nullable = false)
    override val id: Long = 0,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "lic_condition_id", nullable = false)
    val licenceCondition: LicenceCondition,

    @ManyToOne
    @JoinColumn(name = "transfer_status_id", nullable = false)
    override var status: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "accepted_decision_id")
    override var decision: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "allocation_reason_id")
    override val allocationReason: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "rejection_reason_id")
    override var rejectionReason: ReferenceData? = null,

    @Column
    override var requestDate: ZonedDateTime,

    @Column(name = "transfer_status_date")
    override var statusDate: ZonedDateTime? = null,

    @Lob
    @Column
    override var notes: String? = null,

    @ManyToOne
    @JoinColumn(name = "origin_team_id")
    override val originTeam: Team,

    @ManyToOne
    @JoinColumn(name = "origin_staff_id")
    override val originStaff: Staff,

    @ManyToOne
    @JoinColumn(name = "receiving_team_id")
    override val receivingTeam: Team,

    @ManyToOne
    @JoinColumn(name = "receiving_staff_id")
    override val receivingStaff: Staff,

    @Column
    override val masterTransferId: Long? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @CreatedBy
    @Column(nullable = false)
    var createdByUserId: Long = 0,

    @LastModifiedBy
    @Column(nullable = false)
    var lastUpdatedUserId: Long = 0,

    @CreatedDate
    @Column(nullable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()
) : Transfer {
    override val component: SentenceComponent
        get() = licenceCondition
}