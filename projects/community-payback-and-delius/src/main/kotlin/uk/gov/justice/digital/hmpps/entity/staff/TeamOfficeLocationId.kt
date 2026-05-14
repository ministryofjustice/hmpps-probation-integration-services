package uk.gov.justice.digital.hmpps.entity.staff

import java.io.Serializable

data class TeamOfficeLocationId(
    val teamId: Long = 0,
    val officeLocationId: Long = 0
) : Serializable

