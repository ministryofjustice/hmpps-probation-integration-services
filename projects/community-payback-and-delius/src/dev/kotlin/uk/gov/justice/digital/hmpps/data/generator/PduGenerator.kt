package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.staff.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.entity.staff.Provider

object PduGenerator {
    val DEFAULT_PROVIDER_PDU = generatePdu(code = "N01PDU", provider = ProviderGenerator.DEFAULT_PROVIDER)
    val SECOND_PROVIDER_PDU = generatePdu(code = "N02PDU", provider = ProviderGenerator.SECOND_PROVIDER)

    fun generatePdu(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        provider: Provider,
        selectable: Boolean = true,
    ) = ProbationDeliveryUnit(
        id = id,
        code = code,
        provider = provider,
        selectable = selectable,
    )
}

