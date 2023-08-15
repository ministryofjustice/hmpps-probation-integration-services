package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object NotificationGenerator {
    val PRISONER_RELEASED = ResourceLoader.notification<HmppsDomainEvent>("prisoner-released")
    val PRISONER_RECEIVED = ResourceLoader.notification<HmppsDomainEvent>("prisoner-received")
    val PRISONER_DIED = ResourceLoader.notification<HmppsDomainEvent>("prisoner-died")
    val PRISONER_MATCHED = ResourceLoader.notification<HmppsDomainEvent>("prisoner-matched")
    val PRISONER_NEW_CUSTODY = ResourceLoader.notification<HmppsDomainEvent>("prisoner-received-new-custody")
    val PRISONER_RECALLED = ResourceLoader.notification<HmppsDomainEvent>("prisoner-received-recalled")
    val PRISONER_HOSPITAL_RELEASED = ResourceLoader.notification<HmppsDomainEvent>("prisoner-received-hospital-released")
    val PRISONER_HOSPITAL_IN_CUSTODY = ResourceLoader.notification<HmppsDomainEvent>("prisoner-received-hospital-custody")
    val PRISONER_ROTL_RETURN = ResourceLoader.notification<HmppsDomainEvent>("prisoner-received-rotl")
}
