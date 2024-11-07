package uk.gov.justice.digital.hmpps.api.model.appointment

data class AppointmentDetail(
    val appointments: List<CreatedAppointment>,
)

data class CreatedAppointment(
    val id: Long
)
