package uk.gov.justice.digital.hmpps.integrations.delius.team.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.entity.ProbationArea

@Entity
@Immutable
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long = 0,
    @Column(columnDefinition = "char(6)")
    val code: String,
    @Column
    val description: String,
    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val probationArea: ProbationArea,
)

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByCodeAndProbationAreaId(
        code: String,
        probationAreaId: Long,
    ): Team?
}

fun TeamRepository.getByCodeAndProbationAreaId(
    code: String,
    probationAreaId: Long,
) =
    findByCodeAndProbationAreaId(code, probationAreaId) ?: throw NotFoundException("Team", "code", code)
