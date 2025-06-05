package uk.gov.justice.digital.hmpps.integrations.delius.provider

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.user.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.user.StaffWithTeamUser
import java.time.ZonedDateTime

@MappedSuperclass
abstract class StaffRecord(
    @Id
    @Column(name = "staff_id")
    val id: Long = 0,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    val forename: String,
    val surname: String,

    @Column(name = "FORENAME2", length = 35)
    val middleName: String? = null,

    @Column(name = "end_date")
    val endDate: ZonedDateTime? = null,

    @ManyToOne
    @JoinColumn(name = "staff_grade_id")
    val grade: ReferenceData? = null,

    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")]
    )
    val teams: List<Team> = mutableListOf()
) {
    @Transient
    val displayName = listOfNotNull(forename, middleName, surname).joinToString(" ")
}

@Immutable
@Entity
@Table(name = "staff")
class Staff(
    id: Long,
    code: String,
    forename: String,
    surname: String,
    middleName: String? = null,
    endDate: ZonedDateTime? = null,
    grade: ReferenceData? = null,
    teams: List<Team> = mutableListOf()
) : StaffRecord(id, code, forename, surname, middleName, endDate, grade, teams)

@Immutable
@Entity
@Table(name = "staff")
class StaffWithUser(
    id: Long,
    code: String,
    forename: String,
    surname: String,
    middleName: String? = null,
    endDate: ZonedDateTime? = null,
    grade: ReferenceData? = null,
    @OneToOne(mappedBy = "staff")
    val user: StaffUser? = null,
    teams: List<Team> = mutableListOf()
) : StaffRecord(id, code, forename, surname, middleName, endDate, grade, teams)

@Immutable
@Entity
@Table(name = "staff")
class StaffWithTeams(
    @Id
    @Column(name = "staff_id")
    val id: Long,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    @OneToOne(mappedBy = "staff")
    val user: StaffWithTeamUser? = null,

    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")]
    )
    val teams: List<TeamWithDistrict> = mutableListOf(),

    @Column(name = "end_date")
    val endDate: ZonedDateTime? = null,
)
