package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.staff.OfficeLocation
import uk.gov.justice.digital.hmpps.utils.Extensions.reportMissing
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "upw_allocation")
@Immutable
class UnpaidWorkAllocation(
    @Id
    @Column(name = "upw_allocation_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "upw_details_id")
    val details: UnpaidWorkDetails,

    @ManyToOne
    @JoinColumn(name = "upw_project_id")
    val project: UnpaidWorkProject,

    @ManyToOne
    @JoinColumn(name = "upw_project_availability_id")
    val projectAvailability: UnpaidWorkProjectAvailability?,

    @ManyToOne
    @JoinColumn(name = "allocation_day_id")
    val allocationDay: UnpaidWorkDay,

    @ManyToOne
    @JoinColumn(name = "requested_frequency_id")
    val requestedFrequency: ReferenceData?,

    @Column(name = "allocation_start_date")
    val startDate: LocalDate?,

    @Column(name = "allocation_end_date")
    val endDate: LocalDate?,

    @Column(name = "start_time")
    val startTime: LocalTime,

    @Column(name = "end_time")
    val endTime: LocalTime,

    @Column(name = "pick_up_time")
    val pickUpTime: LocalTime?,

    @ManyToOne
    @JoinColumn(name = "pick_up_location_id")
    val pickUpLocation: OfficeLocation?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val rowVersion: Long
)

interface UpwAllocationRepository : JpaRepository<UnpaidWorkAllocation, Long> {
    @Query(
        """
            select a from UnpaidWorkAllocation a
            where a.details.disposal.event.id = :eventId
            and a.softDeleted = false
            and a.details.softDeleted = false
            and a.details.disposal.softDeleted = false
        """
    )
    fun findByEventId(eventId: Long): List<UnpaidWorkAllocation>
    fun findByIdIn(id: Set<Long>): List<UnpaidWorkAllocation>
    fun getByIdIn(ids: List<Long>) =
        ids.toSet().let { ids -> findByIdIn(ids).associateBy { it.id }.reportMissing(ids) }
}

