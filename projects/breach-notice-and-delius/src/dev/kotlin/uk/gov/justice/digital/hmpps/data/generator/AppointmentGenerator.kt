package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.DateTimeGenerator.zonedDateTime
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.DEFAULT_RQMNT
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateContactOutcome
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateContactType
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator.DEFAULT_STAFF
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator.DEFAULT_LOCATION
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.integrations.delius.*
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object AppointmentGenerator {
    val APPOINTMENT_CONTACT_TYPE = generateContactType("APPT1", attendanceContact = true)
    val APPOINTMENT_OUTCOME = generateContactOutcome("AOUT")

    val FUTURE_APPOINTMENTS = listOf(
        generateAppointment(DEFAULT_PERSON, APPOINTMENT_CONTACT_TYPE, zonedDateTime().plusDays(7), DEFAULT_RQMNT),
        generateAppointment(DEFAULT_PERSON, APPOINTMENT_CONTACT_TYPE, zonedDateTime().plusDays(14), DEFAULT_RQMNT),
        generateAppointment(DEFAULT_PERSON, APPOINTMENT_CONTACT_TYPE, zonedDateTime().plusDays(21), DEFAULT_RQMNT),
    )

    val OTHER_APPOINTMENTS = listOf(
        generateAppointment(
            DEFAULT_PERSON,
            APPOINTMENT_CONTACT_TYPE,
            zonedDateTime().plusDays(7),
            DEFAULT_RQMNT,
            outcome = APPOINTMENT_OUTCOME
        ),
        generateAppointment(
            DEFAULT_PERSON,
            APPOINTMENT_CONTACT_TYPE,
            zonedDateTime().minusDays(14),
            DEFAULT_RQMNT,
            outcome = APPOINTMENT_OUTCOME
        ),
        generateAppointment(
            DEFAULT_PERSON,
            APPOINTMENT_CONTACT_TYPE,
            zonedDateTime().minusDays(7),
            DEFAULT_RQMNT
        ),
    )

    fun generateAppointment(
        person: Person,
        type: ContactType,
        dateTime: ZonedDateTime,
        requirement: Requirement,
        team: Team = DEFAULT_TEAM,
        staff: Staff = DEFAULT_STAFF,
        location: OfficeLocation? = DEFAULT_LOCATION,
        outcome: ContactOutcome? = null,
        description: String? = null,
        notes: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Contact(
        person,
        type,
        dateTime.toLocalDate(),
        dateTime.truncatedTo(ChronoUnit.SECONDS),
        requirement.disposal.event,
        requirement,
        team,
        staff,
        location,
        outcome,
        description,
        notes,
        softDeleted,
        id
    )
}