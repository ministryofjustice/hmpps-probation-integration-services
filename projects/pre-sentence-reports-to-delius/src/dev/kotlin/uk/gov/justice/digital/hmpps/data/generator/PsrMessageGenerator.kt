package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.SimpleHmppsEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object PsrMessageGenerator {
    val PSR_MESSAGE = ResourceLoader.message<SimpleHmppsEvent>("psr-message")
}
