package uk.gov.justice.digital.hmpps.api.model

import java.time.ZonedDateTime

data class CreateAppointment(val start: ZonedDateTime, val end: ZonedDateTime) {
    val description = "Some Description for RP"
    val notes = "Resettlement Passport Notes"
}