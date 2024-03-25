package uk.gov.justice.digital.hmpps.api.model.overview

data class Activity(
    val acceptableAbsences: Int,
    val complied: Int,
    val nationalStandardsAppointments: Int,
    val rescheduled: Int
)