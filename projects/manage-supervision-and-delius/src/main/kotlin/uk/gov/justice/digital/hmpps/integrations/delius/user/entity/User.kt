package uk.gov.justice.digital.hmpps.integrations.delius.user.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.annotations.Subselect
import org.hibernate.annotations.Synchronize
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.DisposalType
import java.time.LocalDate
import java.time.ZonedDateTime

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
    val roleCode: String,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns(
        JoinColumn(name = "offender_id", referencedColumnName = "offender_id", insertable = false, updatable = false),
        JoinColumn(name = "staff_employee_id", referencedColumnName = "staff_id", insertable = false, updatable = false)
    )
    val nextAppointment: NextAppointment? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns(
        JoinColumn(name = "offender_id", referencedColumnName = "offender_id", insertable = false, updatable = false),
        JoinColumn(name = "staff_employee_id", referencedColumnName = "staff_id", insertable = false, updatable = false)
    )
    val previousAppointment: PreviousAppointment? = null,

    @ManyToOne
    @JoinColumn(name = "offender_id", insertable = false, updatable = false)
    val latestSentence: LatestSentence? = null

)

@Entity
@Subselect(
    """
        select sub.* 
        from
          (select e.*,
            count(e.event_id) over (partition by e.offender_id) as total_sentences,
            row_number() over (partition by e.offender_id order by cast(e.event_number as number) desc) as row_num 
            from event e join disposal d on d.event_id = e.event_id
            where e.soft_deleted = 0 
            and e.active_flag = 1
            ) sub
        where sub.row_num = 1
"""
)
@Synchronize("event")
data class LatestSentence(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "event_id", referencedColumnName = "event_id", insertable = false, updatable = false)
    val disposal: Disposal? = null,

    @Column(name = "total_sentences")
    val totalNumberOfSentences: Long,
)

@Entity
@Subselect(
    """
        select * from (
            select 
            q.staff_id,
            q.offender_id,
            q.appointment_datetime,
            q.contact_id,
            q.contact_type_id,
            row_number() over (partition by staff_id, offender_id order by appointment_datetime asc) as row_num 
            from
            (
                select 
                to_timestamp(to_char(c.CONTACT_DATE, 'yyyy-mm-dd') || ' ' || to_char(c.CONTACT_START_TIME, 'hh24:mi:ss'), 'yyyy-mm-dd hh24:mi:ss') as appointment_datetime,
                c.contact_id, 
                c.contact_type_id,
                c.staff_id,
                c.offender_id
                from contact c
                join r_contact_type ct on c.contact_type_id = ct.contact_type_id and ct.attendance_contact = 'Y'
                where c.CONTACT_START_TIME is not null
                and c.soft_deleted = 0
            ) q
            where appointment_datetime > current_timestamp
        )
        where row_num = 1
    """
)
@Synchronize("contact")
data class NextAppointment(

    @Id
    @Column(name = "contact_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "staff_id")
    val staffId: Long,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Column(name = "appointment_datetime")
    val appointmentDatetime: ZonedDateTime,

    )

@Entity
@Subselect(
    """
        select * from (
            select 
            q.staff_id,
            q.offender_id,
            q.appointment_datetime,
            q.contact_id,
            q.contact_type_id,
            row_number() over (partition by staff_id, offender_id order by appointment_datetime desc) as row_num 
            from
            (
                select 
                to_timestamp(to_char(c.contact_date, 'yyyy-mm-dd') || ' ' || to_char(c.contact_start_time, 'hh24:mi:ss'), 'yyyy-mm-dd hh24:mi:ss') as appointment_datetime,
                c.contact_id, 
                c.contact_type_id,
                c.staff_id,
                c.offender_id
                from contact c
                join r_contact_type ct on c.contact_type_id = ct.contact_type_id and ct.attendance_contact = 'Y'
                where c.CONTACT_START_TIME is not null
                and c.soft_deleted = 0
            ) q
            where appointment_datetime < current_timestamp
        )
        where row_num = 1
    """
)
@Synchronize("contact")
data class PreviousAppointment(

    @Id
    @Column(name = "contact_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "staff_id")
    val staffId: Long,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Column(name = "appointment_datetime")
    val appointmentDatetime: ZonedDateTime,

    )

interface CaseloadRepository : JpaRepository<Caseload, Long> {
    @Query(
        """
        select c from Caseload c
        join fetch c.person p
        join fetch c.team t
        left join fetch c.nextAppointment na
        left join fetch c.previousAppointment pa
        where c.staff.code = :staffCode
    """
    )
    fun findByStaffCode(staffCode: String, pageable: Pageable): Page<Caseload>

    @Query(
        """
        select c from Caseload c
        join fetch c.person p
        join fetch c.team t
        left join fetch c.nextAppointment na
        left join fetch na.type naType
        left join fetch c.previousAppointment pa
        left join fetch pa.type paType
        left join fetch c.latestSentence ls
        left join fetch ls.disposal d
        left join fetch d.type dt
        where c.staff.code = :staffCode
        and (:nameOrCrn is null 
          or upper(p.crn) like '%' || upper(:nameOrCrn) || '%' 
          or upper(p.forename || ' ' || p.surname) like '%' || upper(:nameOrCrn) || '%'
          or upper(p.surname || ' ' || p.forename) like '%' || upper(:nameOrCrn) || '%'
          or upper(p.surname || ', ' || p.forename) like '%' || upper(:nameOrCrn) || '%')
        and (:nextContactCode is null or (upper(trim(naType.code)) = upper(trim(:nextContactCode))))
        and (:sentenceCode is null or (upper(trim(dt.code)) = upper(trim(:sentenceCode))))
    """
    )
    fun searchByStaffCode(
        staffCode: String,
        nameOrCrn: String?,
        nextContactCode: String?,
        sentenceCode: String?,
        pageable: Pageable
    ): Page<Caseload>

    @Query(
        """
        select c from Caseload c
        join fetch c.person p
        join fetch c.team t
        where c.team.code = :teamCode
    """
    )
    fun findByTeamCode(teamCode: String, pageable: Pageable): Page<Caseload>

    @Query(
        """
            select distinct cont.type from Caseload c
            join Contact cont on cont.personId = c.person.id
            where c.staff.code = :staffCode and cont.type.attendanceContact = true
            order by cont.type.description asc
        """
    )
    fun findContactTypesForStaff(staffCode: String): List<ContactType>

    @Query(
        """
            select distinct e.disposal.type from Caseload c
            join Event e on e.personId = c.person.id and e.active = true and e.softDeleted = false 
            where e.disposal is not null 
            and c.staff.code = :staffCode
            order by e.disposal.type.description asc
        """
    )
    fun findSentenceTypesForStaff(staffCode: String): List<DisposalType>
}

enum class CaseloadOrderType(val sortColumn: String) {
    NEXT_CONTACT("naType.description"),
    LAST_CONTACT("paType.description"),
    SENTENCE("dt.description"),
    SURNAME("p.surname"),
    NAME_OR_CRN("p.surname"),
    DOB("p.dateOfBirth")
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

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate
)
