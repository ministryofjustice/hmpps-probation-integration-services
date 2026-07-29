package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Entity
@Immutable
@Table(name = "caseload")
@SQLRestriction("role_code = 'OM'")
class Caseload(

    @Id
    @Column(name = "caseload_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "staff_employee_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "trust_provider_team_id")
    val team: Team,

    @Column(name = "role_code")
    val roleCode: String,

    @Column(name = "trust_provider_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val trustProviderFlag: Boolean = false,
)

interface CaseloadRepository : JpaRepository<Caseload, Long> {
    @EntityGraph(attributePaths = ["person.gender", "person.manager.team", "person.manager.staff.user"])
    @Query(
        "select c from Caseload c where c.staff.id = :staffId and c.team.code = :teamCode",
        countQuery = "select count(distinct c.id) from Caseload c where c.staff.id = :staffId and c.team.code = :teamCode"
    )
    fun findByStaffIdAndTeamCodeIn(staffId: Long, teamCode: String?, pageable: Pageable): Page<Caseload>

    @EntityGraph(attributePaths = ["person.gender", "person.manager.team", "person.manager.staff.user"])
    @Query(
        "select c from Caseload c where c.staff.id = :staffId",
        countQuery = "select count(distinct c.id) from Caseload c where c.staff.id = :staffId"
    )
    fun findByStaffIdIn(staffId: Long, pageable: Pageable): Page<Caseload>
}