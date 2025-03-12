package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Provider

object ProviderGenerator {
    val DEFAULT_PROVIDER = Provider(IdGenerator.getAndIncrement(), "N01")
}