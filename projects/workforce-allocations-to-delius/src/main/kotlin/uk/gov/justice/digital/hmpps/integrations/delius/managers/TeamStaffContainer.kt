package uk.gov.justice.digital.hmpps.integrations.delius.managers

data class TeamStaffContainer(
    val team: Team,
    val staff: Staff,
    val reason: AllocationReason
)
