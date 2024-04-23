package uk.gov.justice.digital.hmpps.api.model.user

data class TeamStaff(
    val provider: String?,
    val staff: List<Staff>,
)
