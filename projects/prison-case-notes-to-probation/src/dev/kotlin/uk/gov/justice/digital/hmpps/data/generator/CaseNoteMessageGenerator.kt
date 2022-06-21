package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.ResourceLoader

object CaseNoteMessageGenerator {
    val EXISTS_IN_DELIUS = ResourceLoader.caseNoteMessage("case-note-exists-in-delius")
    val NEW_TO_DELIUS = ResourceLoader.caseNoteMessage("case-note-new-to-delius")
}