package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.person.Address
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

data class AppointmentResponse(
    val id: Long,
    val reference: UUID?,
    val version: UUID,
    val project: Project,
    val projectType: CodeName,
    val case: AppointmentResponseCase,
    val event: EventResponse,
    val supervisor: Supervisor,
    val team: CodeName,
    val provider: CodeName,
    val pickUpData: PickUp?,
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

data class AppointmentResponseCase(
    val crn: String,
    val name: PersonName,
    val dateOfBirth: LocalDate,
    val currentExclusion: Boolean,
    val exclusionMessage: String?,
    val currentRestriction: Boolean,
    val restrictionMessage: String?
)

data class AppointmentResponseEnforcementAction(
    val code: String,
    val description: String,
    val respondBy: LocalDate?
)
