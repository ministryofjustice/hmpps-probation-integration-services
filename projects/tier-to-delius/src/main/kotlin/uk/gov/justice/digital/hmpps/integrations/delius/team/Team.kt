package uk.gov.justice.digital.hmpps.integrations.delius.team

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
@Immutable
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long = 0,

    @Column(columnDefinition = "char(6)")
    val code: String,
)
