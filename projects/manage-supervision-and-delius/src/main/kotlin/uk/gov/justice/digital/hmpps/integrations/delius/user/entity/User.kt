package uk.gov.justice.digital.hmpps.integrations.delius.user.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,

    @Column(name = "distinguished_name")
    val username: String,

    @Column
    val forename: String,

    @Column
    val surname: String,
)

@Immutable
@Entity
@Table(name = "staff")
class Staff(

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    @Column
    val forename: String,

    @Column
    val surname: String,

    @JoinColumn(name = "probation_area_id")
    @ManyToOne
    val provider: Provider,

    @OneToMany(mappedBy = "staff")
    val caseLoad: List<Caseload> = emptyList(),

    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")]
    )
    val teams: List<Team> = emptyList(),

    @Id
    @Column(name = "staff_id")
    val id: Long
) {
    fun isUnallocated(): Boolean {
        return code.endsWith("U")
    }
}

@Entity
@Immutable
@Table(name = "team")
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    @Column(name = "description")
    val description: String,

    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "team_id")],
        inverseJoinColumns = [JoinColumn(name = "staff_id")]
    )
    val staff: List<Staff>
)

@Immutable
@Entity
@Table(name = "probation_area")
class Provider(
    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,

    val description: String,

    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(name = "end_date")
    val endDate: LocalDate? = null
)

@Entity
@Immutable
@Table(name = "caseload")
data class Caseload(
    @Id
    @Column(name = "caseload_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: CaseloadPerson,

    @ManyToOne
    @JoinColumn(name = "staff_employee_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "trust_provider_team_id")
    val team: Team
)

@Entity
@Immutable
@Table(name = "offender")
class CaseloadPerson(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "first_name", length = 35)
    val forename: String,

    @Column(name = "second_name", length = 35)
    val secondName: String? = null,

    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,

    @Column(name = "surname", length = 35)
    val surname: String,
)

interface UserRepository : JpaRepository<User, Long> {
    fun findUserByUsername(username: String): User?
}

fun UserRepository.getUser(username: String) =
    findUserByUsername(username) ?: throw NotFoundException("User", "username", username)
