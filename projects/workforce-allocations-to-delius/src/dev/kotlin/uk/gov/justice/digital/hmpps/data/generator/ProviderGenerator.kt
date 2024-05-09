package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.entity.LocalAdminUnit
import uk.gov.justice.digital.hmpps.data.entity.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider

object ProviderGenerator {
    val DEFAULT = Provider(
        IdGenerator.getAndIncrement(),
        "N02",
        "NPS North East"
    )
    val PDU = ProbationDeliveryUnit(id = IdGenerator.getAndIncrement(), code = "PDU1", description = "Some PDU")
    val LAU = LocalAdminUnit(id = IdGenerator.getAndIncrement(), pdu = PDU)
}
