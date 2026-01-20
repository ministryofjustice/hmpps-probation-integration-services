package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "upw_allocation")
@Immutable
class UpwAllocation(
    @Id
    @Column(name = "upw_allocation_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "upw_details_id")
    val details: UpwDetails,

    @ManyToOne
    @JoinColumn(name = "upw_project_id")
    val project: UpwProject,

    @ManyToOne
    @JoinColumn(name = "upw_project_availability_id")
    val projectAvailability: UpwProjectAvailability?,

    @ManyToOne
    @JoinColumn(name = "allocation_day_id")
    val allocationDay: UpwDay,

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

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val rowVersion: Long
)

interface UpwAllocationRepository : JpaRepository<UpwAllocation, Long> {
    @Query(
        """
        select a from UpwAllocation a
        where a.details.disposal.event.id = :eventId
        and a.softDeleted = false
        and a.details.softDeleted = false
        and a.details.disposal.softDeleted = false
    """
    )
    fun findByEventId(eventId: Long): List<UpwAllocation>
}

@Entity
@Table(name = "upw_day")
@Immutable
class UpwDay(
    @Id
    @Column(name = "upw_day_id")
    val id: Long,

    val weekDay: String
)
