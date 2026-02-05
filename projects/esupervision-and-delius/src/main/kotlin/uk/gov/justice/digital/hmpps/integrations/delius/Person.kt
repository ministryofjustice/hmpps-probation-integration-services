package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException.Companion.orIgnore
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

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(name = "first_name")
    val firstName: String,

    @Column(name = "surname")
    val lastName: String,

    @Column(name = "mobile_number")
    val mobile: String?,

    @Column(name = "e_mail_address")
    val emailAddress: String?
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
    fun findByPersonCrnIn(crns: List<String>): List<PersonManager>
    fun getByCrn(crn: String) = findByPersonCrn(crn).orIgnore { "CRN not found" }
}

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrn(crn: String): Person?
    fun existsByCrn(crn: String): Boolean
}

