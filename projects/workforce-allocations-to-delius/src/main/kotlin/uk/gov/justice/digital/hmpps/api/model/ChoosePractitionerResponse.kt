package uk.gov.justice.digital.hmpps.api.model

data class ChoosePractitionerResponse(
    val crn: String,
    val name: Name,
    val probationStatus: ProbationStatus,
    val communityPersonManager: Manager?,
    val teams: Map<String, List<StaffMember>>,
)
