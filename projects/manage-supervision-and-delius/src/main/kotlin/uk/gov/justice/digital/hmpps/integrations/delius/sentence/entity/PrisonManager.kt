package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Provider
import java.time.LocalDate

@Entity
@Table(name = "prison_offender_manager")
class PrisonManager(
    @Id
    @Column(name = "prison_offender_manager_id", nullable = false)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id", nullable = false)
    val staff: Staff,

    val allocationDate: LocalDate,

    val endDate: LocalDate? = null,

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "prisonManager")
    val responsibleOfficers: List<ResponsibleOfficer> = emptyList(),

) {
    fun responsibleOfficer(): ResponsibleOfficer? = responsibleOfficers.firstOrNull { it.isActive() }
}

interface PrisonManagerRepository : CrudRepository<PrisonManager, Long> {
    @Query(
        """
        SELECT om
        FROM PrisonManager om
        WHERE om.person.id = :id
        """
    )
    fun findPrisonManagersByPersonId(id: Long): List<PrisonManager>
}
