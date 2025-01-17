package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object CaseNoteMessageGenerator {
    val EXISTS_IN_DELIUS: Notification<HmppsDomainEvent> =
        ResourceLoader.notification<HmppsDomainEvent>("case-note-exists-in-delius")
    val NEW_TO_DELIUS: Notification<HmppsDomainEvent> =
        ResourceLoader.notification<HmppsDomainEvent>("case-note-new-to-delius")
    val NOT_FOUND: Notification<HmppsDomainEvent> = ResourceLoader.notification<HmppsDomainEvent>("case-note-not-found")
    val RESETTLEMENT_PASSPORT: Notification<HmppsDomainEvent> =
        ResourceLoader.notification<HmppsDomainEvent>("resettlement-passport-casenote")
    val NOMS_NUMBER_ADDED: Notification<HmppsDomainEvent> =
        ResourceLoader.notification<HmppsDomainEvent>("noms-number-added")
}
