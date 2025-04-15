package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

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
    @Convert(converter = NumericBooleanConverter::class)
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