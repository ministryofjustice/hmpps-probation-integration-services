package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import uk.gov.justice.digital.hmpps.utils.Extensions.reportMissing
import java.time.ZonedDateTime

@Entity
@Table(name = "upw_details")
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
class UnpaidWorkDetails(
    @Id
    @Column(name = "upw_details_id")
    val id: Long,

    @Version
    var rowVersion: Long = 0,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @ManyToOne
    @JoinColumn(name = "upw_status_id")
    var status: ReferenceData?,

    @Column(name = "upw_status_date")
    var statusDate: ZonedDateTime,

    @Column(columnDefinition = "number")
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
)

interface UpwDetailsRepository : JpaRepository<UnpaidWorkDetails, Long> {
    @EntityGraph(attributePaths = ["disposal.upwRequirements", "disposal.upwRequirements.requirementSubCategory"])
    @Query(
        """
            select distinct d from UnpaidWorkDetails d
            where d.disposal.event.id = :eventId
            and d.softDeleted = false
            and d.disposal.softDeleted = false
        """
    )
    fun findByEventIdIn(eventId: Long): List<UnpaidWorkDetails>

    @EntityGraph(attributePaths = ["disposal.upwRequirements", "disposal.upwRequirements.requirementSubCategory"])
    @Query(
        """
            select distinct d from UnpaidWorkDetails d
            where d.disposal.event.id in :eventId
            and d.softDeleted = false
            and d.disposal.softDeleted = false
        """
    )
    fun findByEventIdIn(eventId: Collection<Long>): List<UnpaidWorkDetails>
    fun getByEventIdIn(ids: Collection<Long>) =
        ids.toSet().let { ids -> findByEventIdIn(ids).associateBy { it.disposal.event.id }.reportMissing(ids) }
}
