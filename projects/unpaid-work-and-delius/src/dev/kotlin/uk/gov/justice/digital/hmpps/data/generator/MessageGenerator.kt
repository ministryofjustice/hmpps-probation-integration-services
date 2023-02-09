package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val UPW_COMPLETE = ResourceLoader.message<HmppsDomainEvent>("upw-assessment-complete")
}
