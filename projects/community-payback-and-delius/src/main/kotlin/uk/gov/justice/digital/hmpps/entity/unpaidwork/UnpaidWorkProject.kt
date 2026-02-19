package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.person.Address
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import java.time.DayOfWeek
import java.time.LocalDate

@Entity
@Table(name = "upw_project")
@Immutable
class UnpaidWorkProject(
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
    val availability: List<UnpaidWorkProjectAvailability>,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "high_visibility_vest_required")
    val hiVisRequired: Boolean,

    val expectedEndDate: LocalDate?,

    val completionDate: LocalDate?,

    val beneficiary: String?,
    val beneficiaryContactName: String?,
    val beneficiaryEmailAddress: String?,
    val beneficiaryUrl: String?,

    @ManyToOne
    @JoinColumn(name = "beneficiary_contact_address_id")
    val beneficiaryContactAddress: Address?,
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

interface UnpaidWorkProjectRepository : JpaRepository<UnpaidWorkProject, Long> {
    @EntityGraph(attributePaths = ["placementAddress", "beneficiaryContactAddress", "team.provider", "projectType"])
    fun findByCode(code: String): UnpaidWorkProject?

    @EntityGraph(attributePaths = ["placementAddress", "beneficiaryContactAddress", "team.provider", "projectType"])
    fun findAllByIdIn(ids: Collection<Long>): List<UnpaidWorkProject>
}

fun UnpaidWorkProjectRepository.getByCode(code: String) = findByCode(code).orNotFoundBy("code", code)