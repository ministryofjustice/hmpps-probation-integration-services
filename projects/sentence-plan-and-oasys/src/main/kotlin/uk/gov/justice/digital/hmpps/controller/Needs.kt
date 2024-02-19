package uk.gov.justice.digital.hmpps.controller

data class Needs(
    val criminogenicNeeds: List<Need> = listOf()
)

data class Need(val key: String, val description: String)
