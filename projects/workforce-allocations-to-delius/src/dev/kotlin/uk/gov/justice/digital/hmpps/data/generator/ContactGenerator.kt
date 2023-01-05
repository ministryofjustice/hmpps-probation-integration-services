package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactType
import java.time.ZonedDateTime

object ContactGenerator {
    val INITIAL_APPOINTMENT = generate(
        ContactTypeGenerator.INITIAL_APPOINTMENT_IN_OFFICE,
        PersonGenerator.DEFAULT.id,
        EventGenerator.DEFAULT.id,
        TeamGenerator.DEFAULT.id,
        StaffGenerator.DEFAULT.id,
        ProviderGenerator.DEFAULT.id,
    )

    fun generate(
        type: ContactType,
        personId: Long,
        eventId: Long,
        teamId: Long,
        staffId: Long,
        providerId: Long,
        date: ZonedDateTime = ZonedDateTime.now(),
        id: Long = IdGenerator.getAndIncrement(),
    ) = Contact(
        id = id,
        type = type,
        personId = personId,
        eventId = eventId,
        date = date,
        startTime = date,
        teamId = teamId,
        staffId = staffId,
        providerId = providerId,
        notes = "Test notes",
    )
}
