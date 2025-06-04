package uk.gov.justice.digital.hmpps.api.model.user

data class Provider(
    val code: String,
    val name: String,
)

data class ProviderResponse(
    val providers: List<Provider>,
)