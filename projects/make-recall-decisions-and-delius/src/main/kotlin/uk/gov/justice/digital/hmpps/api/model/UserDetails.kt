package uk.gov.justice.digital.hmpps.api.model

data class UserDetails(
    val name: Name,
    val username: String,
    val email: String?,
    val homeArea: Provider?,
    val staffCode: String?,
)
