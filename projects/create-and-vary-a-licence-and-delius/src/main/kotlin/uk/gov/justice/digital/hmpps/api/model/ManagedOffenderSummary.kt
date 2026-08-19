package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class ManagedOffenderSummary(
    val crn: String,
    val nomisId: String? = null,
    val name: Name,
    val allocationDate: LocalDate? = null,
    val staff: StaffSummary,
    val team: TeamSummary? = null,
)

data class StaffSummary(
    val code: String,
    val name: Name? = null,
    val unallocated: Boolean? = false,
)

data class TeamSummary(
    val code: String,
    val description: String,
)


