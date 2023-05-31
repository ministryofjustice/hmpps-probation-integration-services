package uk.gov.justice.digital.hmpps.integrations.randm

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.ZonedDateTime
import java.util.UUID

data class ReferralSession(
    val id: UUID,
    val sessionNumber: Int,
    val appointmentTime: ZonedDateTime,
    @JsonAlias("deliusAppointmentId")
    val deliusId: Long?,
    val sessionFeedback: SessionFeedback
)

data class SessionFeedback(
    val attendance: Attendance,
    val behaviour: Behaviour
)

data class Attendance(
    val attended: String
)

// Non-null behaviour with nullable notify to match model from Interventions Service
data class Behaviour(
    val notifyProbationPractitioner: Boolean?
)

data class SupplierAssessment(
    val id: UUID,
    val appointments: List<Appointment>,
    val currentAppointmentId: UUID?,
    val referralId: UUID
) {
    val currentAppointment = appointments.firstOrNull { it.id == currentAppointmentId }
}

data class Appointment(
    val id: UUID,
    val sessionFeedback: SessionFeedback
)
