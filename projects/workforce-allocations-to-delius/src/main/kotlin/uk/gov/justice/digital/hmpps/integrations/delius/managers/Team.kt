package uk.gov.justice.digital.hmpps.integrations.delius.managers

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Immutable
@Entity
class Team(

    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    val description: String,
)
