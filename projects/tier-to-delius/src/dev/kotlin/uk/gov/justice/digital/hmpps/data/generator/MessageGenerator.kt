package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.listener.TierChangeEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val DEFAULT = ResourceLoader.message<TierChangeEvent>("tier-calculation")
}
