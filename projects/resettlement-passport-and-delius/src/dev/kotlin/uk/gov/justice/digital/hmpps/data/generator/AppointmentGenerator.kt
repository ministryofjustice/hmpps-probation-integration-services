package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.api.model.CreateAppointment
import uk.gov.justice.digital.hmpps.entity.*
import java.time.LocalDate
import java.time.ZonedDateTime

object AppointmentGenerator {
    val ATTENDANCE_TYPE = generateType("AT", attendanceType = true)
    val NON_ATTENDANCE_TYPE = generateType("NAT", attendanceType = false)
    val ATTENDED_OUTCOME = generateOutcome("AO")
    val NON_ATTENDED_OUTCOME = generateOutcome("NAO")
    val DEFAULT_LOCATION = generateLocation(
        "DEFAULT",
        buildingNumber = "1",
        streetName = "Mantle Place",
        townCity = "Heath",
        postcode = "H34 7TH"
    )
    val APPOINTMENT_TYPES = CreateAppointment.Type.entries.map { generateType(it.code, attendanceType = true) }

    fun generateType(
        code: String,
        description: String = "Description for $code",
        attendanceType: Boolean,
        id: Long = IdGenerator.getAndIncrement()
    ) = AppointmentType(code, description, attendanceType, id)

    fun generateOutcome(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = AppointmentOutcome(code, description, id)

    fun generateLocation(
        code: String,
        description: String = "Description of Location $code",
        buildingName: String? = null,
        buildingNumber: String? = null,
        streetName: String? = null,
        district: String? = null,
        townCity: String? = null,
        county: String? = null,
        postcode: String? = null,
        telephoneNumber: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Location(
        code,
        description,
        buildingName,
        buildingNumber,
        streetName,
        district,
        townCity,
        county,
        postcode,
        telephoneNumber,
        id
    )

    fun generate(
        person: Person,
        type: AppointmentType,
        date: LocalDate,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime?,
        externalReference: String? = null,
        location: Location?,
        notes: String? = null,
        probationAreaId: Long,
        team: Team,
        staff: Staff,
        outcome: AppointmentOutcome? = null,
        description: String? = null,
        softDeleted: Boolean = false,
        version: Long = 0
    ) = Appointment(
        person,
        type,
        date,
        startTime,
        endTime,
        notes,
        probationAreaId,
        team,
        staff,
        externalReference,
        description,
        location,
        outcome,
        softDeleted,
        version,
    )
}
