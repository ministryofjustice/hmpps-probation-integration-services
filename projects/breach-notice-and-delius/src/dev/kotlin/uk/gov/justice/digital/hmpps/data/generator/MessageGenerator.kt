package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val BREACH_NOTICE_ADDED = ResourceLoader.message<HmppsDomainEvent>("breach-notice-added")
}
