package uk.gov.justice.digital.hmpps.integrations.delius.provider

import org.hibernate.annotations.Immutable
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass

@Immutable
@Entity
@IdClass(StaffTeamId::class)
class StaffTeam(

    @Id
    @Column(name = "staff_id")
    val staffId: Long,

    @Id
    @Column(name = "team_id")
    val teamId: Long,
)

data class StaffTeamId(val staffId: Long = 0, val teamId: Long = 0) : Serializable
