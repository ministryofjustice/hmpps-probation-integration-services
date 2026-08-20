package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import java.io.Serializable

data class TeamOfficeLocationId(
    val team: Long = 0,
    val officeLocation: Long = 0,
) : Serializable

@Immutable
@Entity
@Table(name = "team_office_location")
@IdClass(TeamOfficeLocationId::class)
class TeamOfficeLocation(
    @Id
    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @Id
    @ManyToOne
    @JoinColumn(name = "office_location_id")
    val officeLocation: OfficeLocation,

    @Column(name = "default_team_location_flag", columnDefinition = "char(1)")
    @Convert(converter = YesNoConverter::class)
    val defaultTeamLocation: Boolean = true,
)

