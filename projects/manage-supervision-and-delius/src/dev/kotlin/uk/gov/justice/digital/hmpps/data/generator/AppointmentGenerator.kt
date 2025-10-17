package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_STAFF
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.generateOutcome
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.AppointmentOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ContactTypeOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ContactTypeOutcomeId
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.SentenceAppointment
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
    val POP_RESCHEDULED_OUTCOME =
        generateOutcome(AppointmentOutcome.Code.RESCHEDULED_POP.value, "Rescheduled - PoP Request", false, true)
    val SERVICE_RESCHEDULED_OUTCOME =
        generateOutcome(AppointmentOutcome.Code.RESCHEDULED_SERVICE.value, "Rescheduled - Service Request", false, true)

    val CONTACT_TYPE_OUTCOMES = APPOINTMENT_TYPES.map {
        generateContactTypeOutcome(it.id, ATTENDED_COMPLIED.id, it, ATTENDED_COMPLIED)
    }

    val PERSON_APPOINTMENT = generateAppointment(
        PersonGenerator.OVERVIEW,
        ZonedDateTime.of(2024, 11, 27, 9, 0, 0, 0, EuropeLondon),
        ZonedDateTime.of(2024, 11, 27, 10, 0, 0, 0, EuropeLondon),
        USER.staff?.id!!
    )

    fun generateAppointment(
        person: Person,
        start: ZonedDateTime,
        end: ZonedDateTime,
        staffId: Long = DEFAULT_STAFF.id,
        teamId: Long = DEFAULT_TEAM.id,
        locationId: Long? = null,
        notes: String? = "Notes",
        sensitive: Boolean? = false,
        outcome: ContactOutcome? = null,
    ) = SentenceAppointment(
        person = person,
        type = APPOINTMENT_TYPES[0],
        date = start.toLocalDate(),
        startTime = start,
        teamId = teamId,
        staffId = staffId,
        officeLocationId = locationId,
        endTime = end,
        externalReference = "externalReference",
        description = "Description",
        softDeleted = false,
        notes = notes,
        sensitive = sensitive,
        outcomeId = outcome?.id
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
