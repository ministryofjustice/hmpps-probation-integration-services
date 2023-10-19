package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.CaseNote
import java.time.LocalDate
import java.time.ZonedDateTime

object CourtCaseNoteGenerator {
    val CASE_NOTE = CaseNote(
        IdGenerator.getAndIncrement(),
        "2222",
        PersonGenerator.CURRENTLY_MANAGED.id,
        ContactTypeGenerator.CONTACT_TYPE,
        "Existing notes",
        LocalDate.now(),
        ZonedDateTime.now(),
        StaffGenerator.ALLOCATED.id,
        StaffGenerator.ALLOCATED.id,
        1,
        1,
        lastModifiedDateTime = ZonedDateTime.now().minusMinutes(1)
    )
}
