package uk.gov.justice.digital.hmpps.model

import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(
    "crn",
    "firstName",
    "mobileNumber",
    "appointmentDate",
    "appointmentTimes",
    "nextWorkSessionProjectType",
    "today",
    "sendSmsForDay",
    "fullName",
    "numberOfEvents",
    "activeUpwRequirements",
    "custodialStatus",
    "currentRemandStatus",
    "allowSms",
    "originalMobileNumber",
    "upwMinutesRemaining"
)
interface UnpaidWorkAppointment {
    val crn: String
    val firstName: String
    val mobileNumber: String
    val appointmentDate: String
    val appointmentTimes: String
    val nextWorkSessionProjectType: String
    val today: String
    val sendSmsForDay: String
    val fullName: String
    val numberOfEvents: String
    val activeUpwRequirements: String
    val custodialStatus: String?
    val currentRemandStatus: String?
    val allowSms: String
    val originalMobileNumber: String
    val upwMinutesRemaining: String
}