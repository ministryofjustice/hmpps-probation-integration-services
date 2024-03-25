package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.Name

@Entity
@Immutable
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
data class Person(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_id")
    val id: Long,
)

@Immutable
@Entity
@SQLRestriction("active_flag = 1")
@Table(name = "offender_manager")
class PersonManager(

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
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
)

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    @EntityGraph(attributePaths = ["person", "provider", "team", "staff.user"])
    fun findByPersonCrn(crn: String): PersonManager?
}

fun PersonManagerRepository.getForCrn(crn: String) =
    findByPersonCrn(crn) ?: throw NotFoundException("Person", "crn", crn)