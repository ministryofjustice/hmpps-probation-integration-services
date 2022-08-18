package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val PRISONER_RELEASED = ResourceLoader.message<HmppsEvent>("prisoner-released")
    val PRISONER_RECEIVED = ResourceLoader.message<HmppsEvent>("prisoner-received")
}
