package uk.gov.justice.digital.hmpps.integrations.delius.team

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea

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
