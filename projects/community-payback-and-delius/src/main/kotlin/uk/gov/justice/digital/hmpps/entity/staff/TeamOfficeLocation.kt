package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.*

@IdClass(TeamOfficeLocationId::class)
@Entity
@Table(name = "team_office_location")
class TeamOfficeLocation(
    @Id
    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @Id
    @ManyToOne
    @JoinColumn(name = "office_location_id")
    val officeLocation: OfficeLocation,
)