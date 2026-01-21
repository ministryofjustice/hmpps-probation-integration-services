package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal

@Entity
@Immutable
@Table(name = "upw_details")
@SQLRestriction("soft_deleted = 0")
class UnpaidWorkDetails(
    @Id
    @Column(name = "upw_details_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface UpwDetailsRepository : JpaRepository<UnpaidWorkDetails, Long> {
    @Query(
        """
        select d from UnpaidWorkDetails d
        where d.disposal.event.id = :eventId
        and d.softDeleted = false
        and d.disposal.softDeleted = false
    """
    )
    fun findByEventId(eventId: Long): List<UnpaidWorkDetails>

    @Query(
        """
        select d from UnpaidWorkDetails d
        where d.disposal.event.id in :eventId
        and d.softDeleted = false
        and d.disposal.softDeleted = false
    """
    )
    fun findAllByEventId(eventId: Collection<Long>): List<UnpaidWorkDetails>
}
