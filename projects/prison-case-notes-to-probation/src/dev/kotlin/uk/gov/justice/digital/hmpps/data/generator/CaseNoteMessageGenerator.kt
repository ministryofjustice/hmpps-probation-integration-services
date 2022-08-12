package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.prison.PrisonOffenderEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object CaseNoteMessageGenerator {
    val EXISTS_IN_DELIUS: PrisonOffenderEvent = ResourceLoader.message("case-note-exists-in-delius")
    val NEW_TO_DELIUS: PrisonOffenderEvent = ResourceLoader.message("case-note-new-to-delius")
    val NOT_FOUND: PrisonOffenderEvent = ResourceLoader.message("case-note-not-found")
}
