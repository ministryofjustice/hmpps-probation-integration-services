package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.ENFORCEMENT_STAFF
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
            "COAP" -> generateType(it.code, attendanceType = true, locationRequired = "Y", contactOutcomeFlag = true)
            "COVC" -> generateType(it.code, attendanceType = true, locationRequired = "Y", contactOutcomeFlag = true)
            "COPT" -> generateType(it.code, attendanceType = true, locationRequired = "B")
            "CHVS" -> generateType(it.code, attendanceType = true, locationRequired = "N")
            else -> generateType(it.code, attendanceType = true, locationRequired = "Y")
        }
    }

    // The COVC type has contactOutcomeFlag=true — used to test enforcementFlag is set on future appointments
    val CONTACT_OUTCOME_FLAG_TYPE get() = APPOINTMENT_TYPES.first { it.code == "COVC" }

    fun generateType(
        code: String,
        description: String = "Description for $code",
        attendanceType: Boolean,
        offenderContact: Boolean = false,
        locationRequired: String,
        editable: Boolean = true,
        contactOutcomeFlag: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = ContactType(
        id,
        code,
        attendanceType,
        description,
        locationRequired = locationRequired,
        offenderContact = offenderContact,
        editable = editable,
        contactOutcomeFlag = contactOutcomeFlag,
    )

    val ATTENDED_COMPLIED = generateOutcome("ATTC", "Attended - Complied", true, true)
    val POP_RESCHEDULED_OUTCOME =
        generateOutcome(AppointmentOutcome.Code.RESCHEDULED_POP.value, "Rescheduled - PoP Request", false, true)
    val SERVICE_RESCHEDULED_OUTCOME =
        generateOutcome(AppointmentOutcome.Code.RESCHEDULED_SERVICE.value, "Rescheduled - Service Request", false, true)

    val NON_SELECTABLE_OUTCOME =
        ContactOutcome(
            id = IdGenerator.getAndIncrement(),
            code = "NSOC",
            description = "Non-selectable outcome for overdue filter test",
            outcomeAttendance = true,
            outcomeCompliantAcceptable = true,
            selectable = false
        )

    val CONTACT_TYPE_OUTCOMES = APPOINTMENT_TYPES.map {
        generateContactTypeOutcome(it.id, ATTENDED_COMPLIED.id, it, ATTENDED_COMPLIED)
    }

    val NON_SELECTABLE_APPOINTMENT_TYPE =
        generateType(
            code = "CNSL",
            description = "Non-selectable overdue filter appointment type",
            attendanceType = true,
            locationRequired = "Y",
            contactOutcomeFlag = true
        )

    val NON_SELECTABLE_CONTACT_TYPE_OUTCOME = generateContactTypeOutcome(
        NON_SELECTABLE_APPOINTMENT_TYPE.id,
        NON_SELECTABLE_OUTCOME.id,
        NON_SELECTABLE_APPOINTMENT_TYPE,
        NON_SELECTABLE_OUTCOME
    )

    fun getPersonAppointment() = generateAppointment(
        PersonGenerator.OVERVIEW,
        ZonedDateTime.of(2024, 11, 27, 9, 0, 0, 0, EuropeLondon),
        ZonedDateTime.of(2024, 11, 27, 10, 0, 0, 0, EuropeLondon),
        USER.staff?.id!!
    )

    fun getLateNightAppointment() = generateAppointment(
        PersonGenerator.OVERVIEW,
        ZonedDateTime.of(2024, 11, 27, 23, 0, 0, 0, EuropeLondon),
        ZonedDateTime.of(2024, 11, 27, 23, 30, 0, 0, EuropeLondon),
        USER.staff?.id!!
    )

    fun getSmsAppointment() = SentenceAppointment(
        person = PersonGenerator.SMS_PERSON,
        type = APPOINTMENT_TYPES[0],
        date = ZonedDateTime.of(2024, 11, 27, 9, 0, 0, 0, EuropeLondon).toLocalDate(),
        startTime = ZonedDateTime.of(2024, 11, 27, 9, 0, 0, 0, EuropeLondon),
        endTime = ZonedDateTime.of(2024, 11, 27, 10, 0, 0, 0, EuropeLondon),
        externalReference = "urn:uk:gov:hmpps:manage-supervision-service:appointment:00000000-0000-0000-0000-000000000002",
        description = "Description",
        softDeleted = false,
        notes = "Notes",
        sensitive = false,
        staffId = DEFAULT_STAFF.id,
        teamId = DEFAULT_TEAM.id,
    )

    fun getNonSelectableOverdueAppointment() = SentenceAppointment(
        person = PersonGenerator.SMS_PERSON,
        type = NON_SELECTABLE_APPOINTMENT_TYPE,
        date = ZonedDateTime.of(2022, 11, 27, 7, 0, 0, 0, EuropeLondon).toLocalDate(),
        startTime = ZonedDateTime.of(2022, 11, 27, 7, 0, 0, 0, EuropeLondon),
        endTime = ZonedDateTime.of(2022, 11, 27, 8, 0, 0, 0, EuropeLondon),
        externalReference = "urn:uk:gov:hmpps:manage-supervision-service:appointment:00000000-0000-0000-0000-000000000099",
        description = "Non-selectable overdue test appointment",
        softDeleted = false,
        notes = "Notes",
        sensitive = false,
        staffId = ENFORCEMENT_STAFF.id,
        teamId = DEFAULT_TEAM.id,
        probationAreaId = DEFAULT_PROVIDER.id,
        eventId = null
    ).apply {
        createdByUserId = USER.id
        lastUpdatedUserId = USER.id
    }

    fun getNonSelectableUserDiaryAppointment() = SentenceAppointment(
        person = PersonGenerator.ENFORCEMENT_PERSON,
        type = NON_SELECTABLE_APPOINTMENT_TYPE,
        date = ZonedDateTime.of(2022, 12, 1, 9, 0, 0, 0, EuropeLondon).toLocalDate(),
        startTime = ZonedDateTime.of(2022, 12, 1, 9, 0, 0, 0, EuropeLondon),
        endTime = ZonedDateTime.of(2022, 12, 1, 10, 0, 0, 0, EuropeLondon),
        externalReference = "urn:uk:gov:hmpps:manage-supervision-service:appointment:00000000-0000-0000-0000-000000000098",
        description = "Non-selectable diary test appointment",
        softDeleted = false,
        notes = "Notes",
        sensitive = false,
        staffId = DEFAULT_STAFF.id,
        teamId = DEFAULT_TEAM.id,
        probationAreaId = DEFAULT_PROVIDER.id,
        eventId = null
    ).apply {
        createdByUserId = USER.id
        lastUpdatedUserId = USER.id
    }

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
        eventId: Long? = PersonGenerator.EVENT_1.id,
        visorContact: Boolean? = null,
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
        outcomeId = outcome?.id,
        probationAreaId = DEFAULT_PROVIDER.id,
        eventId = eventId,
        visorContact = visorContact
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
