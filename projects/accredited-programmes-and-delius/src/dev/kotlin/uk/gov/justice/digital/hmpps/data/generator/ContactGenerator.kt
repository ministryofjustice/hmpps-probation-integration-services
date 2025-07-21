package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.entity.sentence.Event
import java.time.LocalDate

object ContactGenerator {
    fun generate(event: Event, type: ContactType, date: LocalDate) = Contact(
        id = id(),
        date = date,
        type = type,
        event = event,
        softDeleted = false
    )
}
