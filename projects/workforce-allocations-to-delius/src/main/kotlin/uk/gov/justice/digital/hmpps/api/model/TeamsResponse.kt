package uk.gov.justice.digital.hmpps.api.model

data class TeamsResponse(
    val teams: Map<String, List<StaffMember>>
)
