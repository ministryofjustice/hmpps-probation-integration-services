package uk.gov.justice.digital.hmpps.api.model.appointment

import java.time.ZonedDateTime

data class CheckAppointment(
    val start: ZonedDateTime,
    val end: ZonedDateTime,
)
