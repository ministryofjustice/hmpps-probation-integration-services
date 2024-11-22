package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object NotificationGenerator {
    val PRISONER_RELEASED = ResourceLoader.notification<HmppsDomainEvent>("prisoner-released")
    val PRISONER_RECEIVED = ResourceLoader.notification<HmppsDomainEvent>("prisoner-received")
    val PRISONER_DIED = ResourceLoader.notification<HmppsDomainEvent>("prisoner-died")
    val PRISONER_MATCHED = ResourceLoader.notification<HmppsDomainEvent>("prisoner-matched")
    val PRISONER_MATCHED_WITH_POM = ResourceLoader.notification<HmppsDomainEvent>("prisoner-matched-with-pom")
    val PRISONER_NEW_CUSTODY = ResourceLoader.notification<HmppsDomainEvent>("prisoner-received-new-custody")
    val PRISONER_RECALLED = ResourceLoader.notification<HmppsDomainEvent>("prisoner-received-recalled")
    val PRISONER_HOSPITAL_RELEASED =
        ResourceLoader.notification<HmppsDomainEvent>("prisoner-received-hospital-released")
    val PRISONER_HOSPITAL_IN_CUSTODY =
        ResourceLoader.notification<HmppsDomainEvent>("prisoner-received-hospital-custody")
    val PRISONER_ROTL_RETURN = ResourceLoader.notification<HmppsDomainEvent>("prisoner-received-rotl")
    val PRISONER_IRC_RELEASED = ResourceLoader.notification<HmppsDomainEvent>("prisoner-received-irc-released")
    val PRISONER_IRC_IN_CUSTODY = ResourceLoader.notification<HmppsDomainEvent>("prisoner-received-irc-custody")
    val PRISONER_RELEASED_ECSL_ACTIVE = ResourceLoader.notification<HmppsDomainEvent>("prisoner-released-ecsl-active")
    val PRISONER_RELEASED_ECSL_INACTIVE =
        ResourceLoader.notification<HmppsDomainEvent>("prisoner-released-ecsl-inactive")
    val PRISONER_ABSCONDED = ResourceLoader.notification<HmppsDomainEvent>("prisoner-absconded")
    val PRISONER_ETR_IN_CUSTODY = ResourceLoader.notification<HmppsDomainEvent>("prisoner-received-etr-custody")
    val PRISONER_ECSLIRC_IN_CUSTODY = ResourceLoader.notification<HmppsDomainEvent>("prisoner-received-ecslirc-custody")
    val PRISONER_ADMIN_MERGE = ResourceLoader.notification<HmppsDomainEvent>("prisoner-released-admin-merge")
    val PRISONER_RELEASED_HISTORIC = ResourceLoader.notification<HmppsDomainEvent>("prisoner-released-historic")
}
