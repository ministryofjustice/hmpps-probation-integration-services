package uk.gov.justice.digital.hmpps.model

data class Provider(
    val code: String,
    val description: String,
)

data class ProviderWithLaus(
    val code: String,
    val description: String,
    val localAdminUnits: List<LocalAdminUnit>,
)
