package uk.gov.justice.digital.hmpps.model

data class SessionResponse(
    val project: Project,
    val appointmentSummaries: List<SessionResponseAppointmentSummary>
)

data class SessionResponseAppointmentSummary(
    val id: Long,
    val case: AppointmentResponseCase,
    val outcome: CodeDescription?,
    val requirementProgress: RequirementProgress
)
