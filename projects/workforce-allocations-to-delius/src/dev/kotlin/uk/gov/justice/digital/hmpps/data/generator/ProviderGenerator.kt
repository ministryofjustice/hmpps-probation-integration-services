package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.provider.Borough
import uk.gov.justice.digital.hmpps.integrations.delius.provider.District
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider

object ProviderGenerator {
    val DEFAULT = Provider(
        IdGenerator.getAndIncrement(),
        "N02",
        "NPS North East"
    )
    val PDU =
        Borough(id = IdGenerator.getAndIncrement(), code = "PDU1", description = "Some PDU", probationArea = DEFAULT)
    val LAU = District(id = IdGenerator.getAndIncrement(), code = "LAU1", description = "Some LAU", borough = PDU)
}
