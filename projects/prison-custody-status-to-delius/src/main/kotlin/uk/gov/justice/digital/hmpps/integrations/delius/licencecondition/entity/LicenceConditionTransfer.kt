package uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.staff.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.entity.Team
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "lic_condition_transfer")
@SQLRestriction("soft_deleted = 0")
class LicenceConditionTransfer(
    @Id
    @Column(name = "lic_condition_transfer_id", nullable = false)
    val id: Long = 0,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "lic_condition_id", nullable = false)
    val licenceCondition: LicenceCondition,

    @ManyToOne
    @JoinColumn(name = "transfer_status_id", nullable = false)
    var status: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "accepted_decision_id")
    var decision: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "allocation_reason_id")
    val reason: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "rejection_reason_id")
    var rejectionReason: ReferenceData? = null,

    @Column
    var requestDate: ZonedDateTime,

    @Column(name = "transfer_status_date")
    var statusDate: ZonedDateTime? = null,

    @Lob
    @Column
    var notes: String? = null,

    @ManyToOne
    @JoinColumn(name = "origin_team_id")
    val originTeam: Team,

    @ManyToOne
    @JoinColumn(name = "origin_staff_id")
    val originStaff: Staff,

    @ManyToOne
    @JoinColumn(name = "receiving_team_id")
    val receivingTeam: Team,

    @ManyToOne
    @JoinColumn(name = "receiving_staff_id")
    val receivingStaff: Staff,

    @Column
    val masterTransferId: Long? = null,

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
)

interface LicenceConditionTransferRepository : JpaRepository<LicenceConditionTransfer, Long> {
    fun findAllByLicenceConditionIdAndStatusCode(
        licenceConditionId: Long,
        statusCode: String
    ): List<LicenceConditionTransfer>
}
