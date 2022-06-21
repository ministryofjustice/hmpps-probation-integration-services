package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.ResourceLoader

object NomisCaseNoteGenerator {
    val EXISTING_IN_BOTH = ResourceLoader.nomisCaseNote("get-case-note-body-exists-in-delius")

    val NEW_TO_DELIUS = ResourceLoader.nomisCaseNote("get-case-note-body-new-to-delius")
}
