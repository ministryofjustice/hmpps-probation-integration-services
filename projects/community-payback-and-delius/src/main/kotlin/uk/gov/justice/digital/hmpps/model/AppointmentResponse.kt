package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.person.Address
import uk.gov.justice.digital.hmpps.entity.staff.OfficeLocation
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

data class AppointmentResponse(
    val id: Long,
    val reference: UUID?,
    val version: UUID,
    val project: Project,
    val projectType: NameCode,
    val case: AppointmentResponseCase,
    val event: EventResponse,
    val supervisor: AppointmentResponseSupervisor,
    val team: NameCode,
    val provider: NameCode,
    val pickUpData: AppointmentResponsePickupData?,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val penaltyHours: String,
    val outcome: CodeDescription?,
    val enforcementAction: AppointmentResponseEnforcementAction?,
    val hiVisWorn: Boolean?,
    val workedIntensively: Boolean?,
    val workQuality: WorkQuality?,
    val behaviour: Behaviour?,
    val notes: String?,
    val updatedAt: ZonedDateTime,
    val sensitive: Boolean?,
    val alertActive: Boolean?
)

data class AppointmentResponseAddress(
    val streetName: String?,
    val buildingName: String?,
    val addressNumber: String?,
    val townCity: String?,
    val county: String?,
    val postCode: String?
)

fun Address.toAppointmentResponseAddress() = AppointmentResponseAddress(
    this.streetName,
    this.buildingName,
    this.addressNumber,
    this.town,
    this.county,
    this.postcode
)

fun OfficeLocation.toAppointmentResponseAddress() = AppointmentResponseAddress(
    this.streetName,
    this.buildingName,
    this.buildingNumber,
    this.town,
    this.county,
    this.postcode
)

data class NameCode(
    val name: String,
    val code: String
)

data class AppointmentResponseName(
    val forename: String,
    val surname: String,
    val middleNames: List<String>
)

data class AppointmentResponseCase(
    val crn: String,
    val name: AppointmentResponseName,
    val dateOfBirth: LocalDate,
    val currentExclusion: Boolean,
    val exclusionMessage: String?,
    val currentRestriction: Boolean,
    val restrictionMessage: String?
)

data class AppointmentResponseSupervisor(
    val code: String,
    val name: AppointmentResponseName
)

data class AppointmentResponsePickupData(
    val location: AppointmentResponseAddress?,
    val time: LocalTime?
)

data class AppointmentResponseEnforcementAction(
    val code: String,
    val description: String,
    val respondBy: LocalDate?
)
