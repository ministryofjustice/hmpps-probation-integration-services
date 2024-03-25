package uk.gov.justice.digital.hmpps.integrations.randm

import com.fasterxml.jackson.annotation.JsonAlias
import uk.gov.justice.digital.hmpps.service.NoSessionReasonType
import java.time.ZonedDateTime
import java.util.UUID

data class Appointment(
    val id: UUID,
    val appointmentFeedback: AppointmentFeedback
) {
    fun attendanceRecorded() = appointmentFeedback.attendanceFeedback.attended != null
    fun attendanceSubmittedAt() = appointmentFeedback.attendanceFeedback.submittedAt
}

data class AppointmentFeedback(val attendanceFeedback: AttendanceFeedback, val sessionFeedback: SessionFeedback)

// Non-null behaviour with nullable attended to match model from Interventions Service
data class AttendanceFeedback(
    val attended: String?,
    val didSessionHappen: Boolean?,
    val submittedAt: ZonedDateTime?
)

// Non-null behaviour with nullable notify to match model from Interventions Service
data class SessionFeedback(
    val noSessionReasonType: NoSessionReasonType?,
    @JsonAlias("notifyProbationPractitionerOfBehaviour")
    val notifyProbationPractitioner: Boolean?
)
