package uk.gov.justice.digital.hmpps.integrations.delius.user.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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

interface UserRepository : JpaRepository<User, Long> {

    @Query(
        """
        select u
        from User u
        join fetch u.staff s
        join fetch s.provider p
        where upper(u.username) = upper(:username)
    """
    )
    fun findByUsername(username: String): User?
}

fun UserRepository.getUser(username: String) =
    findByUsername(username) ?: throw NotFoundException("User", "username", username)

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
    val teams: List<Team>,

    @Id
    @Column(name = "staff_id")
    val id: Long
) {
    fun isUnallocated(): Boolean {
        return code.endsWith("U")
    }
}

interface StaffRepository : JpaRepository<Staff, Long> {
    @Query(
        """
        select s.teams 
        from Staff s
        where s.code = :staffCode
    """
    )
    fun findTeamsByStaffCode(staffCode: String): List<Team>

    @Query(
        """
        select s
        from Staff s
        where s.code = :staffCode
    """
    )
    fun findByStaffCode(staffCode: String): Staff?
}

fun StaffRepository.getStaff(staffCode: String) =
    findByStaffCode(staffCode) ?: throw NotFoundException("Staff", "staffCode", staffCode)

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
    val staff: List<Staff>,

    @JoinColumn(name = "probation_area_id")
    @ManyToOne
    val provider: Provider,

    @Column(name = "end_date")
    val endDate: LocalDate? = null

)

interface TeamRepository : JpaRepository<Team, Long> {
    @Query(
        """
        select t.staff
        from Team t
        where t.code = :teamCode
        and (t.endDate is null or t.endDate > current_date)
    """
    )
    fun findStaffByTeamCode(teamCode: String): List<Staff>

    @Query(
        """
        select t.provider.description
        from Team t
        where t.code = :teamCode
        and (t.endDate is null or t.endDate > current_date)
    """
    )
    fun findProviderByTeamCode(teamCode: String): String?

    @Query(
        """
        select t
        from Team t
        where t.code = :teamCode
        and (t.endDate is null or t.endDate > current_date)
    """
    )
    fun findByTeamCode(teamCode: String): Team?
}

fun TeamRepository.getTeam(teamCode: String) =
    findByTeamCode(teamCode) ?: throw NotFoundException("Team", "teamCode", teamCode)

fun TeamRepository.getProvider(teamCode: String) =
    findProviderByTeamCode(teamCode) ?: throw NotFoundException("Team", "teamCode", teamCode)

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
@SQLRestriction("role_code = 'OM'")
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
    val team: Team,

    @Column(name = "role_code")
    val roleCode: String
)

interface CaseloadRepository : JpaRepository<Caseload, Long> {
    @Query(
        """
        select c from Caseload c
        join fetch c.person p
        join fetch c.team t
        where c.staff.code = :staffCode
    """
    )
    fun findByStaffCode(staffCode: String): List<Caseload>

    @Query(
        """
        select c from Caseload c
        join fetch c.person p
        join fetch c.team t
        where c.team.code = :teamCode
    """
    )
    fun findByTeamCode(teamCode: String, pageable: Pageable): Page<Caseload>
}

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



