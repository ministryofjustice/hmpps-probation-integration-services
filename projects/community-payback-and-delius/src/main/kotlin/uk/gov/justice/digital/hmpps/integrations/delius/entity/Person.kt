package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@Entity
@Table(name = "offender")
@Immutable
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long? = null,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "first_name", length = 35)
    val forename: String,

    @Column(name = "second_name", length = 35)
    val secondName: String? = null,

    @Column(name = "surname", length = 35)
    val surname: String,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(name = "current_exclusion", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val currentExclusion: Boolean = false,

    @Column(name = "current_restriction", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val currentRestriction: Boolean = false,

    val exclusionMessage: String? = null,

    val restrictionMessage: String? = null,

    )

@Entity
@Immutable
@Table(name = "offender_manager")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id", nullable = false)
    val id: Long,

    @Column(name = "offender_id", nullable = false)
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id", nullable = false)
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    fun findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(personId: Long): PersonManager?
}

fun PersonManagerRepository.getActiveManagerForPerson(personId: Long) =
    findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(personId) ?: throw NotFoundException(
        "PersonManager",
        "personId",
        personId
    )