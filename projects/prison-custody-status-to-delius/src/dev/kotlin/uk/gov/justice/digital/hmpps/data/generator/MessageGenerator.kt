package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val PRISONER_RELEASED = ResourceLoader.message<HmppsDomainEvent>("prisoner-released")
    val PRISONER_RECEIVED = ResourceLoader.message<HmppsDomainEvent>("prisoner-received")
    val PRISONER_DIED = ResourceLoader.message<HmppsDomainEvent>("prisoner-died")
    val PRISONER_MATCHED = ResourceLoader.message<HmppsDomainEvent>("prisoner-matched")
    val PRISONER_NEW_CUSTODY = ResourceLoader.message<HmppsDomainEvent>("prisoner-received-new-custody")
    val PRISONER_RECALLED = ResourceLoader.message<HmppsDomainEvent>("prisoner-received-recalled")
}
