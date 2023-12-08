package uk.gov.justice.digital.hmpps.api.model

data class AllocationCompletedResponse(
    val crn: String,
    val name: Name,
    val event: Event,
    val type: CaseType,
    val initialAppointment: InitialAppointment?,
    val staff: StaffMember?,
)
