package uk.gov.justice.digital.hmpps.data.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table

@IdClass(TeamOfficeLocationId::class)
@Entity
@Table(name = "team_office_location")
class TeamOfficeLocation(
    @Id
    val teamId: Long,
    @Id
    val officeLocationId: Long,
)