package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Contact
import uk.gov.justice.digital.hmpps.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.entity.ContactType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object ContactGenerator {
    val ENFORCEABLE_CONTACT_OUTCOME_TYPE = ContactOutcomeType(
        id = IdGenerator.getAndIncrement(),
        code = "ENF",
        description = "Enforceable",
        enforceable = true
    )

    val DEFAULT_CONTACT_TYPE = ContactType(
        id = IdGenerator.getAndIncrement(),
        code = "F2FC",
        description = "F2F Contact"
    )

    val DEFAULT_ENFORCEABLE_CONTACT = Contact(
        id = IdGenerator.getAndIncrement(),
        eventId = EventGenerator.DEFAULT_EVENT.eventId,
        date = LocalDate.now(),
        startTime = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 0), ZoneId.systemDefault()),
        softDeleted = false,
        notes = "Some notes",
        outcomeType = ENFORCEABLE_CONTACT_OUTCOME_TYPE,
        type = DEFAULT_CONTACT_TYPE
    )
}