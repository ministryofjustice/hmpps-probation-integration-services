package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNote
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object PrisonCaseNoteGenerator {
    val EXISTING_IN_BOTH = ResourceLoader.file<PrisonCaseNote>("get-case-note-body-exists-in-delius")
    val NEW_TO_DELIUS = ResourceLoader.file<PrisonCaseNote>("get-case-note-body-new-to-delius")
    val RESETTLEMENT_PASSPORT = ResourceLoader.file<PrisonCaseNote>("get-case-note-body-resettlement-passport-new")
}
