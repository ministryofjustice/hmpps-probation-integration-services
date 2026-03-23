package uk.gov.justice.digital.hmpps.data.entity

import java.io.Serializable

data class TeamOfficeLocationId(
    val teamId: Long = 0,
    val officeLocationId: Long = 0
) : Serializable

