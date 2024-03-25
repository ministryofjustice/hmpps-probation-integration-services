package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider

object ProviderGenerator {
    val DEFAULT = Provider(
        IdGenerator.getAndIncrement(),
        "N02",
        "NPS North East"
    )
}
