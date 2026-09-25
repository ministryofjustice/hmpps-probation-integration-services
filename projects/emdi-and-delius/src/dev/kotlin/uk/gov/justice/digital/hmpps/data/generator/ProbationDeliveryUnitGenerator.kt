package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.staff.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.entity.staff.Provider

object ProbationDeliveryUnitGenerator {
    val DEFAULT = generate(code = "PDU001", description = "Central Borough", provider = ProviderGenerator.DEFAULT)
    val SECOND = generate(code = "PDU002", description = "East Borough", provider = ProviderGenerator.DEFAULT)
    val THIRD = generate(code = "PDU003", description = "North Borough", provider = ProviderGenerator.DEFAULT)

    val INACTIVE_PDU = generate(
        code = "PDU050",
        description = "Inactive Borough",
        provider = ProviderGenerator.DEFAULT,
        selectable = false
    )

    val PDU_IN_INACTIVE_PROVIDER = generate(
        code = "PDU060",
        description = "Borough In Inactive Provider",
        provider = ProviderGenerator.INACTIVE
    )

    val LONDON_PDU = generate(code = "LPDU001", description = "Central London", provider = ProviderGenerator.LONDON)

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        provider: Provider,
        selectable: Boolean = true
    ) = ProbationDeliveryUnit(id, code, description, provider, selectable)
}

