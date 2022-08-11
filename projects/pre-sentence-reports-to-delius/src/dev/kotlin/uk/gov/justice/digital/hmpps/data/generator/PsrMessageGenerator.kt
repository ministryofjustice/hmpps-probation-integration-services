package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object PsrMessageGenerator {
    val PSR_MESSAGE = ResourceLoader.message<HmppsEvent>("psr-message")
}
