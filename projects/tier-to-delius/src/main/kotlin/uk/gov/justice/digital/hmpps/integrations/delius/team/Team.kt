package uk.gov.justice.digital.hmpps.integrations.delius.team

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long = 0,
    @Column(columnDefinition = "char(6)")
    val code: String,
)
