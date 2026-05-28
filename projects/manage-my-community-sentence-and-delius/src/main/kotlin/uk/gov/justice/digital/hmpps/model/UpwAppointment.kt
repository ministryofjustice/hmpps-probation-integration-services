package uk.gov.justice.digital.hmpps.model

data class UpwAppointment(
    val minutesCredited: Int? = null,
    val softDeleted: Boolean = false,
    val contact: Appointment,
    val pickUpLocation: Address?,
    val project: UpwProject?,
)