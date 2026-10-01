package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.io.Serializable

@Entity
@Immutable
@Table(name = "staff_team")
@IdClass(StaffTeamId::class)
class StaffTeam(
    @Id
    @Column(name = "staff_id")
    val staffId: Long,

    @Id
    @Column(name = "team_id")
    val teamId: Long,

    @ManyToOne
    @JoinColumn(name = "staff_id", referencedColumnName = "staff_id", insertable = false, updatable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    val team: Team,
)

data class StaffTeamId(
    val staffId: Long = 0,
    val teamId: Long = 0,
) : Serializable

