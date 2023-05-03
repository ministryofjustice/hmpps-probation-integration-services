package uk.gov.justice.digital.hmpps.service

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.Name
import java.time.LocalDate

@Immutable
@Table(name = "offender")
@Entity
@Where(clause = "soft_deleted = 0")
class Person(

    @OneToMany(mappedBy = "person")
    @Where(clause = "active_flag = 1")
    val managers: List<PersonManager> = listOf(),

    @Column(name = "surname", length = 35)
    val surname: String,

    @Column(name = "first_name", length = 35)
    val forename: String,

    @Column(name = "second_name", length = 35)
    val secondName: String? = null,

    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomisId: String,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "current_tier")
    val tier: ReferenceData?
)

@Immutable
@Entity
@Where(clause = "soft_deleted = 0")
@Table(name = "offender_manager")
class PersonManager(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
)

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrn(crn: String): Person?
}

fun PersonRepository.getPerson(crn: String) = findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)

fun Person.name() = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)
