package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.delius.entity.UpwMinutes

data class SessionResponse(
    val project: AppointmentResponseProject,
    val appointmentSummaries: List<SessionResponseAppointmentSummary>
)

data class SessionResponseAppointmentSummary(
    val id: Long,
    val case: AppointmentResponseCase,
    val outcome: CodeDescription?,
    val requirementProgress: UpwMinutes
)
