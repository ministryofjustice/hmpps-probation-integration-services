package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class Conviction(
    val title: String?,
    val offence: String,
    val date: LocalDate,
    val active: Boolean,
    val documents: List<Document>,
    val institutionName: String?,
)
