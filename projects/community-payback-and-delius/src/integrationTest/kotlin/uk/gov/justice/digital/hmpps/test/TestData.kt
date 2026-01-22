package uk.gov.justice.digital.hmpps.test

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.model.*
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

object TestData {
    fun createAppointment() = CreateAppointmentRequest(
        reference = UUID.randomUUID(),
        crn = PersonGenerator.DEFAULT_PERSON.crn,
        eventNumber = UPWGenerator.EVENT_3.number.toInt(),
        date = LocalDate.now().plusDays(1),
        startTime = LocalTime.NOON,
        endTime = LocalTime.NOON.plusHours(6),
        supervisor = Code(StaffGenerator.DEFAULT_STAFF.code),
        allocationId = UPWGenerator.DEFAULT_UPW_ALLOCATION.id,
        pickUp = null,
        outcome = null,
        hiVisWorn = null,
        workedIntensively = null,
        workQuality = null,
        behaviour = null,
        sensitive = false,
        alertActive = false,
        notes = "testing"
    )

    fun createAppointmentWithOutcome() = CreateAppointmentRequest(
        reference = UUID.randomUUID(),
        crn = PersonGenerator.DEFAULT_PERSON.crn,
        eventNumber = UPWGenerator.EVENT_3.number.toInt(),
        date = LocalDate.now().minusDays(1),
        startTime = LocalTime.NOON,
        endTime = LocalTime.NOON.plusHours(6),
        supervisor = Code(StaffGenerator.DEFAULT_STAFF.code),
        allocationId = UPWGenerator.DEFAULT_UPW_ALLOCATION.id,
        pickUp = PickUp(
            location = Code(UPWGenerator.DEFAULT_OFFICE_LOCATION.code),
            time = LocalTime.of(11, 30)
        ),
        minutesCredited = 360,
        penaltyMinutes = 0,
        outcome = Code(ReferenceDataGenerator.ATTENDED_COMPLIED_CONTACT_OUTCOME.code),
        hiVisWorn = true,
        workedIntensively = false,
        workQuality = WorkQuality.SATISFACTORY,
        behaviour = Behaviour.EXCELLENT,
        sensitive = false,
        alertActive = false,
        notes = "testing"
    )

    fun updateAppointment(id: Long) = AppointmentOutcomeRequest(
        id = id,
        version = UUID(1, 1),
        outcome = null,
        supervisor = Code("N01P001"),
        startTime = LocalTime.of(10, 0),
        endTime = LocalTime.of(18, 0),
        notes = "testing update",
        hiVisWorn = true,
        workedIntensively = true,
        penaltyMinutes = 65,
        minutesCredited = 415,
        workQuality = WorkQuality.EXCELLENT,
        behaviour = Behaviour.EXCELLENT,
        sensitive = false,
        alertActive = false,
    )
}