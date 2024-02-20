package uk.gov.justice.digital.hmpps.api.model.overview

import java.time.LocalDate

data class NextAppointment(
    val date: LocalDate,
    val description: String
)