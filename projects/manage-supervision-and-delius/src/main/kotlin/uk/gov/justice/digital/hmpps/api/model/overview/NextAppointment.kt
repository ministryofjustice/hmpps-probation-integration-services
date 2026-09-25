package uk.gov.justice.digital.hmpps.api.model.overview

import java.time.ZonedDateTime

data class Appointment(
    val id: Long,
    val date: ZonedDateTime,
    val description: String
)