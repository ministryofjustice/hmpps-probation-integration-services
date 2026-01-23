package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.person.Address
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "upw_project")
@Immutable
class UpwProject(
    @Id
    @Column(name = "upw_project_id")
    val id: Long,

    val name: String,

    val code: String,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "placement_address_id")
    val placementAddress: Address?,

    @ManyToOne
    @JoinColumn(name = "project_type_id")
    val projectType: ReferenceData,

    @OneToMany(mappedBy = "project")
    val availability: List<UpwProjectAvailability>,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "high_visibility_vest_required")
    val hiVisRequired: Boolean,

    val expectedEndDate: LocalDate?,

    val completionDate: LocalDate?
) {
    fun requireAvailabilityOnDates(dates: List<LocalDate>) = apply {
        require(completionDate == null || completionDate > dates.max()) {
            "Appointment cannot be scheduled after the project completion date (${dates.max()} > $completionDate)"
        }
        if (availability.isNotEmpty()) {
            val availableDays = availability.map { DayOfWeek.valueOf(it.dayOfWeek.weekDay.uppercase()) }.toSet()
            val requestedDays = dates.map { it.dayOfWeek }.toSet()
            val invalidDays = requestedDays - availableDays
            require(invalidDays.isEmpty()) {
                "Project is not available on the following days: $invalidDays"
            }
        }
    }
}

@Entity
@Immutable
@Table(name = "upw_project_availability")
class UpwProjectAvailability(
    @Id
    @Column(name = "upw_project_availability_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "upw_project_id")
    val project: UpwProject,

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

interface UnpaidWorkProjectRepository : JpaRepository<UpwProject, Long> {
    fun findByCode(code: String): UpwProject?
}

fun UnpaidWorkProjectRepository.getByCode(code: String) = findByCode(code).orNotFoundBy("code", code)