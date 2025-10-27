package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.model.CodeDescription
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "team")
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long = 0,

    @Column(columnDefinition = "char(6)")
    val code: String,

    @Column
    val description: String,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @Column(name = "unpaid_work_team")
    @Convert(converter = YesNoConverter::class)
    val unpaidWorkTeam: Boolean,

    @Column(name = "start_date")
    val startDate: LocalDate,

    @Column(name = "end_date")
    val endDate: LocalDate?,

    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "team_id")],
        inverseJoinColumns = [JoinColumn(name = "staff_id")]
    )
    val staff: List<Staff>,
)

interface TeamRepository : JpaRepository<Team, Long> {
    @Query(
        """
            select t
            from Team t
            where t.provider.code = :code
            and t.unpaidWorkTeam = true
            and t.startDate <= current_date
            and (t.endDate is null or t.endDate > current_date)
        """
    )
    fun findUnpaidWorkTeamsByProviderCode(code: String): List<Team>

    @Query(
        """
        select s
        from Team t
        join t.staff s
        where t.code = :teamCode
          and s.startDate <= current_date
          and (s.endDate is null or s.endDate > current_date)
      """
    )
    fun findStaffByTeamCode(teamCode: String): List<Staff>
}

fun Team.toCodeDescription() = CodeDescription(
    code = this.code,
    description = this.description
)