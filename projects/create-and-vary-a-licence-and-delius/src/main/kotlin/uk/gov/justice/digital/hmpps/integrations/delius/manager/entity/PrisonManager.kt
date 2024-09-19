package uk.gov.justice.digital.hmpps.integrations.delius.manager.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team

@Entity
@Table(name = "prison_offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PrisonManager(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @ManyToOne
    @JoinColumn(name = "allocation_team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Id
    @Column(name = "prison_offender_manager_id")
    val id: Long = 0,
)

interface PrisonManagerRepository : JpaRepository<PrisonManager, Long> {
    @EntityGraph(attributePaths = ["person", "provider", "team", "staff.user"])
    fun findAllByPersonCrn(crn: String): List<PrisonManager>
}
