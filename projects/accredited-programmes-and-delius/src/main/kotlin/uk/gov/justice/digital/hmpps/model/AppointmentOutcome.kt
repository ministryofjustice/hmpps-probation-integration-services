package uk.gov.justice.digital.hmpps.model

data class AppointmentOutcome(
    val code: String,
    val description: String,
    val attended: Boolean?,
    val complied: Boolean?,
)