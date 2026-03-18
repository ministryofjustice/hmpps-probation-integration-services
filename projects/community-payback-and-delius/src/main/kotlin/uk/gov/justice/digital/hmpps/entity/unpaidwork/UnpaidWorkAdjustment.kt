package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
@Table(name = "upw_adjustment")
class UnpaidWorkAdjustment(
    @Id
    @Column(name = "upw_adjustment_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "upw_details_id")
    val upwDetails: UnpaidWorkDetails,

    @Column(name = "adjustment_amount")
    var adjustmentAmount: Long,

    @Column(name = "adjustment_date")
    var adjustmentDate: LocalDate,

    @Column(name = "adjustment_type")
    var adjustmentType: String,

    @JoinColumn(name = "adjustment_reason_id")
    @ManyToOne
    var adjustmentReason: ReferenceData,

    @Column(name = "soft_deleted")
    @Convert(converter = NumericBooleanConverter::class)
    var softDeleted: Boolean = false
)

interface UnpaidWorkAdjustmentRepository : JpaRepository<UnpaidWorkAdjustment, Long> {
    @Query(
        """
            select a from UnpaidWorkAdjustment a
            where a.upwDetails.disposal.event.person.crn = :crn
            and a.upwDetails.disposal.event.number = :eventNumber
            and a.softDeleted = false
            and a.upwDetails.softDeleted = false
            and a.upwDetails.disposal.softDeleted = false
        """
    )
    fun findByCrnAndEventNumber(crn: String, eventNumber: String): List<UnpaidWorkAdjustment>
    fun findFirstById(id: Long): UnpaidWorkAdjustment?
}