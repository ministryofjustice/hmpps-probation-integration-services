package uk.gov.justice.digital.hmpps.appointments.test

import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentContact
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Disposal
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.DisposalType
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Event
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.OfficeLocation
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Outcome
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Staff
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Team
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Type
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Type.Companion.REVIEW_ENFORCEMENT_STATUS
import uk.gov.justice.digital.hmpps.appointments.model.CreateAppointment
import uk.gov.justice.digital.hmpps.appointments.model.ReferencedEntities
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

object TestData {
    val PERSON = AppointmentEntities.Person(id(), "A000001")
    val TYPE = Type(id(), "TYPE", attendance = true, nationalStandards = true)
    val REVIEW_TYPE = Type(id(), REVIEW_ENFORCEMENT_STATUS, attendance = false)
    val OUTCOME = Outcome(id(), "ATTC", "Attended - complied", attended = true, complied = true, enforceable = false)
    val FTC_OUTCOME = Outcome(id(), "FTC", "Failed to comply", attended = false, complied = false, enforceable = true)
    val PROVIDER = AppointmentEntities.Provider(id(), "P01")
    val TEAM = Team(id(), "T01", "Team 1", PROVIDER)
    val STAFF = Staff(id(), "S01")
    val OFFICE_LOCATION = OfficeLocation(id(), "LOC1")
    val ACTION = AppointmentEntities.EnforcementAction(id(), "ACT01", "Action 1", 7, TYPE)

    fun createAppointment(
        reference: String = "REF01",
        date: LocalDate = LocalDate.now().plusDays(1),
        startTime: LocalTime = LocalTime.now(),
        outcomeCode: String? = null
    ) = CreateAppointment(
        reference = reference,
        typeCode = TYPE.code,
        relatedTo = ReferencedEntities(personId = PERSON.id),
        date = date,
        startTime = startTime,
        endTime = startTime.plusHours(1),
        staffCode = STAFF.code,
        teamCode = TEAM.code,
        locationCode = OFFICE_LOCATION.code,
        outcomeCode = outcomeCode
    )

    fun appointment(
        personId: Long = PERSON.id,
        date: LocalDate = LocalDate.now(),
        startTime: ZonedDateTime = ZonedDateTime.now(),
        staff: Staff = STAFF,
        team: Team = TEAM,
        type: Type = TYPE,
        outcome: Outcome? = null,
        officeLocation: OfficeLocation? = OFFICE_LOCATION,
        event: Event? = null,
        externalReference: String? = "REF01",
        notes: String? = null,
        sensitive: Boolean? = false
    ) = AppointmentContact(
        id = id(),
        personId = personId,
        event = event,
        date = date,
        startTime = startTime,
        staff = staff,
        team = team,
        officeLocation = officeLocation,
        type = type,
        outcome = outcome,
        externalReference = externalReference,
        notes = notes,
        sensitive = sensitive
    )

    fun event(
        disposal: Disposal? = null,
        breachEnd: LocalDate? = null,
        ftcCount: Long = 0,
    ) = Event(id(), disposal, ftcCount, breachEnd)

    fun disposal(
        event: Event,
        ftcLimit: Long? = 2
    ) = Disposal(
        id = id(),
        event = event,
        type = DisposalType(id(), "DT01", ftcLimit),
        date = LocalDate.now().minusMonths(1)
    )
}