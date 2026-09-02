package uk.gov.justice.digital.hmpps.entity.staff

import java.io.Serializable

data class TeamOfficeLocationId(
    val team: Long = 0,
    val officeLocation: Long = 0
) : Serializable

