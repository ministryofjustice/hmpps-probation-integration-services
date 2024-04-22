package uk.gov.justice.digital.hmpps.api.model.user

data class Team(
    val description: String,
    val cases: List<StaffCase>
)
