package uk.gov.justice.digital.hmpps.api.model.overview

import java.time.ZonedDateTime

data class OverviewNextAppointment(
    val date: ZonedDateTime,
    val description: String
)

