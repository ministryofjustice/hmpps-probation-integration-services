package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val RECEIVED_A000001 = ResourceLoader.get<HmppsDomainEvent>("esupervision-received-A000001")
    val RECEIVED_A000004 = ResourceLoader.get<HmppsDomainEvent>("esupervision-received-A000004")
    val EXPIRED_A000001 = ResourceLoader.get<HmppsDomainEvent>("esupervision-expired-A000001")
    val SETUP_COMPLETED_A000001 = ResourceLoader.get<HmppsDomainEvent>("esupervision-setup-completed-A000001")
    val SETUP_REMOVED_A000001 = ResourceLoader.get<HmppsDomainEvent>("esupervision-setup-removed-A000001")
    val SETUP_REMOVED_MANUAL_STOP_A000001 =
        ResourceLoader.get<HmppsDomainEvent>("esupervision-setup-removed-manual-stop-A000001")
    val SETUP_REMOVED_NO_ACTIVE_EVENTS_A000001 =
        ResourceLoader.get<HmppsDomainEvent>("esupervision-setup-removed-no-active-events-A000001")
    val SETUP_REMOVED_IN_RESET_A000001 =
        ResourceLoader.get<HmppsDomainEvent>("esupervision-setup-removed-in-reset-A000001")
    val SETUP_REMOVED_NULL_EVENT_A000001 =
        ResourceLoader.get<HmppsDomainEvent>("esupervision-setup-removed-null-event-A000001")
    val SENTENCE_TERMINATED_A000008 = ResourceLoader.get<HmppsDomainEvent>("esupervision-sentence-terminated-A000008")
}
