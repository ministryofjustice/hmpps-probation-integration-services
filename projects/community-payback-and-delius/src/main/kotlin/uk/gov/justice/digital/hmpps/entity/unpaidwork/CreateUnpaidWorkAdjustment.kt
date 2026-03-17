package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.Versioned
import uk.gov.justice.digital.hmpps.entity.staff.OfficeLocation
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "upw_adjustment")
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
class CreateUnpaidWorkAdjustment(
    @Id
    @SequenceGenerator(
        name = "upw_adjustment_id_generator",
        sequenceName = "upw_adjustment_id_seq",
        allocationSize = 1
    )
    @GeneratedId(generator = "upw_adjustment_id_generator")
    @Column(name = "upw_adjustment_id")
    override val id: Long? = null,

    @Version
    override var rowVersion: Long = 0,

    @Column(name = "upw_details_id")
    val detailsId: Long,

    @Column(name = "adjustment_amount")
    var adjustmentAmount: Long,
    @Column(name = "adjustment_date")
    var adjustmentDate: LocalDate,
    @Column(name = "adjustment_type")
    var adjustmentType: String,
    @Column(name = "adjustment_reason_id")
    var adjustmentReasonId: Long,

    @CreatedBy
    val adjustedByUserId: Long,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    var softDeleted: Boolean = false,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(name = "partition_area_id")
    val partitionAreaId: Long = 0,

    ) : Versioned

interface CreateUnpaidWorkAdjustmentRepository : JpaRepository<CreateUnpaidWorkAdjustment, Long> {
    fun findFirstById(id: Long): CreateUnpaidWorkAdjustment?
}