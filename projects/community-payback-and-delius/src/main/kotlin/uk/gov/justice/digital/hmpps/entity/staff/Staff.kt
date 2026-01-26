package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.model.CodeDescription
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.Supervisor
import uk.gov.justice.digital.hmpps.model.SupervisorTeamsResponse
import uk.gov.justice.digital.hmpps.utils.Extensions.reportMissing
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "staff")
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long = 0,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    val forename: String,
    val surname: String,

    @Column(name = "forename2")
    val middleName: String?,

    @ManyToOne
    @JoinColumn(name = "staff_grade_id")
    val grade: ReferenceData?,

    @Column(name = "start_date")
    val startDate: LocalDate,

    @Column(name = "end_date")
    val endDate: LocalDate?,

    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")]
    )
    @SQLRestriction("unpaid_work_team = 'Y'")
    val teams: List<Team>
)

fun Staff.toSupervisor() = Supervisor(
    name = Name(
        forename = this.forename,
        middleName = this.middleName,
        surname = this.surname
    ),
    code = this.code,
    grade = this.grade?.let {
        CodeDescription(
            code = this.grade.code,
            description = this.grade.description
        )
    }
)

fun Staff.toSupervisorTeams() = teams.map {
    SupervisorTeamsResponse(
        code = it.code,
        description = it.description,
        provider = CodeDescription(
            code = it.provider.code,
            description = it.provider.description
        )
    )
}

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findByCode(code: String): Staff?
    fun getByCode(code: String) = findByCode(code).orNotFoundBy("code", code)
    fun findByCodeIn(code: Collection<String>): List<Staff>
    fun getByCodeIn(codes: List<String>) =
        codes.toSet().let { codes -> findByCodeIn(codes).associateBy { it.code }.reportMissing(codes) }
}