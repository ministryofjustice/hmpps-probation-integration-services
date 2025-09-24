package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.generateOutcome
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Appointment
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ContactTypeOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ContactTypeOutcomeId
import java.time.ZonedDateTime

object AppointmentGenerator {

    val APPOINTMENT_TYPES = CreateAppointment.Type.entries.mapNotNull {
        when (it.code) {
            "CODC" -> null
            "COPT" -> generateType(it.code, attendanceType = true, locationRequired = "B")
            "CHVS" -> generateType(it.code, attendanceType = true, locationRequired = "N")
            else -> generateType(it.code, attendanceType = true, locationRequired = "Y")
        }
    }

    fun generateType(
        code: String,
        description: String = "Description for $code",
        attendanceType: Boolean,
        offenderContact: Boolean = false,
        locationRequired: String,
        editable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = ContactType(
        id,
        code,
        attendanceType,
        description,
        locationRequired = locationRequired,
        offenderContact = offenderContact,
        editable = editable,
    )

    val ATTENDED_COMPLIED = generateOutcome("ATTC", "Attended - Complied", true, true)

    val CONTACT_TYPE_OUTCOMES = APPOINTMENT_TYPES.map {
        generateContactTypeOutcome(it.id, ATTENDED_COMPLIED.id, it, ATTENDED_COMPLIED)
    }

    val PERSON_APPOINTMENT = generateAppointment(
        PersonGenerator.OVERVIEW, USER.staff?.id!!, DEFAULT_TEAM.id,
        ZonedDateTime.of(2024, 11, 27, 9, 0, 0, 0, EuropeLondon),
        ZonedDateTime.of(2024, 11, 27, 10, 0, 0, 0, EuropeLondon)
    )

    fun generateAppointment(person: Person, staffId: Long, teamId: Long, start: ZonedDateTime, end: ZonedDateTime) =
        Appointment(
            person = person,
            type = APPOINTMENT_TYPES[0],
            date = start.toLocalDate(),
            startTime = start,
            teamId = teamId,
            staffId = staffId,
            endTime = end,
            externalReference = "externalReference",
            description = "Description",
            softDeleted = false,
            notes = "Notes",
            sensitive = false,
        ).apply {
            createdByUserId = USER.id
            lastUpdatedUserId = USER.id
        }

    fun generateContactTypeOutcome(
        contactTypeId: Long,
        contactOutcomeTypeId: Long,
        contactType: ContactType,
        outcome: ContactOutcome
    ) = ContactTypeOutcome(
        ContactTypeOutcomeId(contactTypeId, contactOutcomeTypeId),
        contactType,
        outcome
    )
}
