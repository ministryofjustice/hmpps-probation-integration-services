package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.*
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object ContactGenerator {

    val DEFAULT_CONTACT = generateContact(
        person = PersonGenerator.DEFAULT_PERSON,
        type = ReferenceDataGenerator.APPOINTMENT_CONTACT_TYPE,
        dateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS),
        id = 2L
    )

    fun generateContact(
        person: Person,
        type: ContactType,
        dateTime: ZonedDateTime,
        id: Long = IdGenerator.getAndIncrement(),
        staff: Staff = StaffGenerator.DEFAULT,
        location: OfficeLocation? = OfficeLocationGenerator.DEFAULT,
        outcome: ContactOutcome? = null,
        description: String? = null,
        notes: String? = null,
        documentLinked: Boolean? = null,
        softDeleted: Boolean = false,
    ) = Contact(
        id = id,
        person = person,
        type = type,
        date = dateTime.toLocalDate(),
        startTime = dateTime.truncatedTo(ChronoUnit.SECONDS),
        staff = staff,
        location = location,
        outcome = outcome,
        description = description,
        notes = notes,
        documentLinked = documentLinked,
        softDeleted = softDeleted
    )
}