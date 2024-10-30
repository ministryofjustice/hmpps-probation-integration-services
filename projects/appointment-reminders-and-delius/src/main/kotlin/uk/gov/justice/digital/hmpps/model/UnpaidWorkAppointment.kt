package uk.gov.justice.digital.hmpps.model

import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(
    "firstName",
    "mobileNumber",
    "appointmentDate",
    "appointmentTimes",
    "crn",
    "eventNumber",
    "upwAppointmentId",
)
interface UnpaidWorkAppointment {
    val firstName: String
    val mobileNumber: String
    val appointmentDate: String
    val appointmentTimes: String
    val crn: String
    val eventNumber: String
    val upwAppointmentId: String
}