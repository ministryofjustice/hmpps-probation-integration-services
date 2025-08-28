package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
@Table(name = "offender")
class Person(
    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
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

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long,
)

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    @EntityGraph(attributePaths = ["provider", "team", "staff"])
    fun findByPersonCrn(crn: String): PersonManager?
}

fun PersonManagerRepository.getByCrn(crn: String) =
    findByPersonCrn(crn) ?: throw NotFoundException("Person", "crn", crn)