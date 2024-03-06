package uk.gov.justice.digital.hmpps.api.model.overview

import java.time.LocalDateTime

data class NextAppointment(
    val date: LocalDateTime,
    val description: String
)