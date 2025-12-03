package uk.gov.justice.digital.hmpps.model

data class Project(
    val name: String,
    val code: String,
    val location: AppointmentResponseAddress?,
    val hiVisRequired: Boolean
)