package uk.gov.justice.digital.hmpps.integrations.randm

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.ZonedDateTime

data class ReferralSession(
    val id: String,
    val sessionNumber: Int,
    val appointmentTime: ZonedDateTime,
    @JsonAlias("deliusAppointmentId")
    val appointmentId: Long,
    val sessionFeedback: SessionFeedback
)

data class SessionFeedback(
    val attendance: Attendance,
    val behaviour: Behaviour
)

data class Attendance(
    val attended: String,
    val submittedAt: ZonedDateTime
)

// Non-null behaviour with nullable notify to match model from Interventions Service
data class Behaviour(
    val notifyProbationPractitioner: Boolean?
)
