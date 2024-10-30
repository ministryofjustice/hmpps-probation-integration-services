package uk.gov.justice.digital.hmpps.model

import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(
    "firstName",
    "mobileNumber",
    "appointmentDate",
    "crn",
    "eventNumbers",
    "upwAppointmentIds",
)
interface UnpaidWorkAppointment {
    val firstName: String
    val mobileNumber: String
    val appointmentDate: String
    val crn: String
    val eventNumbers: String
    val upwAppointmentIds: String
}