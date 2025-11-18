package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.delius.entity.UpwMinutes

data class SessionResponse(
    val project: SessionResponseProject,
    val appointmentSummaries: List<SessionResponseAppointmentSummary>
)

data class SessionResponseProject(
    val name: String,
    val code: String,
    val location: AppointmentResponseAddress?
)

data class SessionResponseAppointmentSummary(
    val id: Long,
    val case: AppointmentResponseCase,
    val outcome: CodeDescription?,
    val requirementProgress: UpwMinutes
)
