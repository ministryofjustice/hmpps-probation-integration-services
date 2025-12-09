package uk.gov.justice.digital.hmpps.appointments.domain.person

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.appointments.domain.provider.Team
import uk.gov.justice.digital.hmpps.appointments.domain.provider.Staff
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
open class Person(
    @Column(columnDefinition = "char(7)")
    val crn: String,

    @OneToOne(mappedBy = "person")
    val manager: PersonManager,

    @Id
    @Column(name = "offender_id")
    val id: Long
)

@Entity
@Immutable
@Table(name = "offender_manager")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
open class PersonManager(
    @OneToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
)

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrn(crn: String): Person?
}

fun PersonRepository.getPerson(crn: String): Person =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)