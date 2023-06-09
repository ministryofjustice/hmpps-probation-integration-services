package uk.gov.justice.digital.hmpps.controller

data class Needs(
    val criminogenicNeeds: List<Need> = listOf()
) 

data class Need(val description: String)