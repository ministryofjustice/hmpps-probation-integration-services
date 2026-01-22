package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.staff.Provider

object ProviderGenerator {
    val DEFAULT_PROVIDER = generateProvider(code = "N01", description = "N01 Provider")
    val SECOND_PROVIDER = generateProvider(code = "N02", description = "N02 Provider")
    val UNSELECTABLE_PROVIDER =
        generateProvider(code = "N50", description = "N50 Inactive Provider", selectable = false)

    fun generateProvider(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        selectable: Boolean = true
    ) = Provider(id, code, description, selectable)
}