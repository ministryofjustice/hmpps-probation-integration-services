package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class Conviction(
    val convictionDate: LocalDate?,
    val outcome: String,
    val offences: List<Offence>
)

data class Offence(
    val description: String,
    val mainOffence: Boolean = false
)

data class ConvictionsContainer(
    val convictions: List<Conviction> = listOf()
)
