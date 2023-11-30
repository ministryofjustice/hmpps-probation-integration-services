package uk.gov.justice.digital.hmpps.service.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.Name
import java.time.LocalDate

@Immutable
@Table(name = "offender")
@Entity
@SQLRestriction("soft_deleted = 0")
class Person(

    @OneToMany(mappedBy = "person")
    @SQLRestriction("active_flag = 1")
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

    @ManyToOne
    @JoinColumn(name = "current_tier")
    val tier: ReferenceData?,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
) {
    val manager
        get() = managers.first()
}

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrn(crn: String): Person?
}

fun PersonRepository.getPerson(crn: String) = findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)

fun Person.name() = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
@Table(name = "offender_manager")
class PersonManager(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
)
