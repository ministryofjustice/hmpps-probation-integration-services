package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.AttendanceOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import java.time.LocalDate

object ContactGenerator {
    val ATTENDANCE_CONTACT_TYPE = ContactType(
        IdGenerator.getAndIncrement(),
        "C295",
        "Attendance Type",
        true,
        true
    )

    val ATTENDANCE_OUTCOME = AttendanceOutcome(
        IdGenerator.getAndIncrement(),
        "Attendance Outcome"
    )

    val ATTENDANCE_CONTACT_1 = generateAttendanceContact(
        PersonGenerator.CURRENTLY_MANAGED.id,
        SentenceGenerator.CURRENTLY_MANAGED.id,
        enforcementContact = true
    )
    val ATTENDANCE_CONTACT_2 = generateAttendanceContact(
        PersonGenerator.CURRENTLY_MANAGED.id,
        SentenceGenerator.CURRENTLY_MANAGED.id,
        enforcementContact = false,
        outcome = ATTENDANCE_OUTCOME
    )

    fun generateAttendanceContact(
        personId: Long,
        eventId: Long?,
        outcome: AttendanceOutcome? = null,
        enforcementContact: Boolean? = null
    ) = Contact(
        id = IdGenerator.getAndIncrement(),
        type = ATTENDANCE_CONTACT_TYPE,
        outcome = outcome,
        date = LocalDate.now().minusDays(1),
        offenderId = personId,
        eventId = eventId,
        enforcementContact = enforcementContact
    )
}
