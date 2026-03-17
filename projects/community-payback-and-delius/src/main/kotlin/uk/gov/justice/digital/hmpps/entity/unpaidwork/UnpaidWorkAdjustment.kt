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

    @Column(name = "upw_details_id")
    val upwDetailsId: Long,

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
            select c.externalReference, a from UnpaidWorkAdjustment a
            join UnpaidWorkDetails u on a.upwDetailsId = u.id
            join Event e on u.disposal.event.id = e.id
            join Contact c on c.contactPerson.id = e.person.id and c.primaryKeyId = e.id
            where e.person.crn = :crn
            and e.number = :eventNumber
            and a.softDeleted = false
        """
    )
    fun findExternalReferenceAndAdjustmentByCrnAndEventNumber(crn: String, eventNumber: String): List<Array<Any>>
    fun findFirstById(id: Long): UnpaidWorkAdjustment?
}