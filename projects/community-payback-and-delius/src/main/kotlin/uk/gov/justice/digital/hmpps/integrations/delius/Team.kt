package uk.gov.justice.digital.hmpps.integrations.delius

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
}

fun Team.toCodeDescription() = CodeDescription(
    code = this.code,
    description = this.description
)