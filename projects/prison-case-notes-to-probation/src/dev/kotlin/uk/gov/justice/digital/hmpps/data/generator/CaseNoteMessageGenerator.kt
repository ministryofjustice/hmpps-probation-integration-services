package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object CaseNoteMessageGenerator {
    val EXISTS_IN_DELIUS: HmppsDomainEvent = ResourceLoader.message("case-note-exists-in-delius")
    val NEW_TO_DELIUS: HmppsDomainEvent = ResourceLoader.message("case-note-new-to-delius")
    val NOT_FOUND: HmppsDomainEvent = ResourceLoader.message("case-note-not-found")
    val RESETTLEMENT_PASSPORT: HmppsDomainEvent = ResourceLoader.message("resettlement-passport-casenote")
}
