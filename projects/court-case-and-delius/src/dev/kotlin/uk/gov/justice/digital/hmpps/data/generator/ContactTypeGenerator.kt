package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.CaseNoteType

object ContactTypeGenerator {
    val CONTACT_TYPE = CaseNoteType(
        IdGenerator.getAndIncrement(),
        "C294",
        "case note type",
        false
    )
}
