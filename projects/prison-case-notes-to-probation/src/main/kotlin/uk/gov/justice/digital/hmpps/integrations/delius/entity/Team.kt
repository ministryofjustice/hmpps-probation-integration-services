package uk.gov.justice.digital.hmpps.integrations.delius.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Immutable
@Entity(name = "team")
class Team(

    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(name = "code")
    val code: String
)
