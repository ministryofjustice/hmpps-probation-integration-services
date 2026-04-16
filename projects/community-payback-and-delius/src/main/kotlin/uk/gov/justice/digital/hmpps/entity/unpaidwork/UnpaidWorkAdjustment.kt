package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.Versioned
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import uk.gov.justice.digital.hmpps.service.AdjustmentService.Companion.REFERENCE_PREFIX
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "upw_adjustment")
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
class UnpaidWorkAdjustment(
    @Id
    @SequenceGenerator(name = "upw_adjustment_id_generator", sequenceName = "upw_adjustment_id_seq", allocationSize = 1)
    @GeneratedId(generator = "upw_adjustment_id_generator")
    @Column(name = "upw_adjustment_id")
    override val id: Long? = null,

    @Version
    override var rowVersion: Long = 0,

    @ManyToOne
    @JoinColumn(name = "upw_details_id")
    val upwDetails: UnpaidWorkDetails,

    @Column(name = "adjustment_amount")
    var amount: Int,

    @Column(name = "adjustment_date")
    var date: LocalDate,

    @Column(name = "adjustment_type")
    var type: String,

    @JoinColumn(name = "adjustment_reason_id")
    @ManyToOne
    var reason: ReferenceData,

    @Column(name = "adjusted_by_user_id")
    var adjustedByUserId: Long,

    @Column(name = "external_reference")
    var externalReference: String? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

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

interface UnpaidWorkAdjustmentRepository : JpaRepository<UnpaidWorkAdjustment, Long> {
    @Query(
        """
            select a from UnpaidWorkAdjustment a
            where a.upwDetails.disposal.event.person.crn = :crn
            and a.upwDetails.disposal.event.number = :eventNumber
        """
    )
    fun findByCrnAndEventNumber(crn: String, eventNumber: String): List<UnpaidWorkAdjustment>
    fun findFirstById(id: Long): UnpaidWorkAdjustment?
    fun findByExternalReference(urn: String): UnpaidWorkAdjustment?
    fun findByReference(uuid: UUID) = findByExternalReference("$REFERENCE_PREFIX$uuid")
}