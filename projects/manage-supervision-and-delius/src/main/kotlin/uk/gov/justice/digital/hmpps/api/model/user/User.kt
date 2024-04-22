package uk.gov.justice.digital.hmpps.api.model.user

data class User(
    val provider: String?,
    val teams: List<Team>,
    val cases: List<StaffCase>
)
