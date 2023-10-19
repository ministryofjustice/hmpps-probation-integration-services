package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object CourtCaseNoteMessageGenerator {
    val EXISTS: HmppsDomainEvent = ResourceLoader.message("existing-court-case-note")
    val NEW: HmppsDomainEvent = ResourceLoader.message("new-court-case-note")
    val NOT_FOUND: HmppsDomainEvent = ResourceLoader.message("court-case-note-not-found")
}
