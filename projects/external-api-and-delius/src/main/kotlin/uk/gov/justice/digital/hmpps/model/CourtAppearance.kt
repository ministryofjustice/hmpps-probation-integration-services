package uk.gov.justice.digital.hmpps.model

import java.time.ZonedDateTime

data class CourtAppearance(
    val type: String,
    val date: ZonedDateTime,
    val court: String,
    val plea: String?,
)
