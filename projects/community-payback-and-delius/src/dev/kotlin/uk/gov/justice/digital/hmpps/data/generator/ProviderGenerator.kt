package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Provider

object ProviderGenerator {
    val DEFAULT_PROVIDER = generateProvider(code = "N01", description = "N01 Provider")
    val SECOND_PROVIDER = generateProvider(code = "N02", description = "N02 Provider")

    fun generateProvider(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String
    ) = Provider(id, code, description)
}