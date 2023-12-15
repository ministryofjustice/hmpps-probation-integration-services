package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val NO_REGISTRATIONS = ResourceLoader.message<HmppsDomainEvent>("assessment-summary-produced-N123456")
}
