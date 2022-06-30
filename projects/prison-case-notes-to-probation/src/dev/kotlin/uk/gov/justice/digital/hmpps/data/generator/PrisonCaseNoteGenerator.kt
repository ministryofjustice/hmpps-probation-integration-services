package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.ResourceLoader

object PrisonCaseNoteGenerator {
    val EXISTING_IN_BOTH = ResourceLoader.prisonCaseNote("get-case-note-body-exists-in-delius")
    val NEW_TO_DELIUS = ResourceLoader.prisonCaseNote("get-case-note-body-new-to-delius")
}
