package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Immutable
@Table(name = "upw_project_availability")
class UnpaidWorkProjectAvailability(
    @Id
    @Column(name = "upw_project_availability_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "upw_project_id")
    val project: UnpaidWorkProject,

    @ManyToOne
    @JoinColumn(name = "upw_day_id")
    val dayOfWeek: UnpaidWorkDay,

    @ManyToOne
    @JoinColumn(name = "frequency_id")
    val frequency: ReferenceData?,

    @Column(name = "start_time")
    val startTime: LocalTime?,

    @Column(name = "end_time")
    val endTime: LocalTime?,

    @Column(name = "start_date")
    val startDate: LocalDate?,

    @Column(name = "end_date")
    val endDate: LocalDate?
)