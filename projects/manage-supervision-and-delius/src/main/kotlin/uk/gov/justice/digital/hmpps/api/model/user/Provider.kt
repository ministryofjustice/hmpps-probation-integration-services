package uk.gov.justice.digital.hmpps.api.model.user

data class Provider(
    val id: Long,
    val code: String,
    val name: String,
)

data class UserProviderResponse(
    val providers: List<Provider>,
)