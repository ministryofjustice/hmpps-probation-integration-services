package uk.gov.justice.digital.hmpps.integrations.delius.provider

import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData

data class TeamStaffContainer(
    val team: Team,
    val staff: Staff,
    val reason: ReferenceData,
)
