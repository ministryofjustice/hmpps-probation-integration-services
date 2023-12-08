package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class Supervision(
    val number: Int,
    val active: Boolean,
    val date: LocalDate?,
    val sentence: Sentence?,
    val mainOffence: Offence?,
    val additionalOffences: List<Offence>?,
    val courtAppearances: List<CourtAppearance>?,
)
