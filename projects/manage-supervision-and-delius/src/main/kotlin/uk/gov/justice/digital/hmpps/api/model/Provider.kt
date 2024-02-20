package uk.gov.justice.digital.hmpps.api.model

data class Provider(
    val code: String,
    val name: String
)

fun uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider.toProvider() = Provider(code, description)
