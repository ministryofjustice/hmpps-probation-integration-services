package uk.gov.justice.digital.hmpps.api.model

data class Overview(
    val personalDetails: PersonalDetailsOverview,
    val registerFlags: List<String>,
    val lastRelease: Release?,
    val activeConvictions: List<Conviction>,
)
