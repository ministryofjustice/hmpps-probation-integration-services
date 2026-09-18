package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.staff.Provider

object ProviderGenerator {
    val DEFAULT = generate(code = "N01", description = "N01 Provider")
    val LONDON = generate(code = "N02", description = "N02 London Provider")
    val NORTH = generate(code = "N03", description = "N03 Northern Provider")
    val INACTIVE = generate(code = "N50", description = "N50 Inactive Provider", selectable = false)

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        selectable: Boolean = true
    ) = Provider(id, code, description, selectable)
}

