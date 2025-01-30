package uk.gov.justice.digital.hmpps.api.model.sentence

import java.time.LocalDate

data class PreviousOrder(
    val eventNumber: String,
    val title: String,
    val description: String?,
    val terminationDate: LocalDate?,
)