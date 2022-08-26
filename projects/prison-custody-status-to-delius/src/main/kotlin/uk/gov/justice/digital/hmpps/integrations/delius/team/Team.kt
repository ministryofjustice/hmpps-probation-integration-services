package uk.gov.justice.digital.hmpps.integrations.delius.team

import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

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
