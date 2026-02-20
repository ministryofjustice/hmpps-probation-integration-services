package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.contact.toCodeDescription
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAllocation
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointment
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkProjectAvailability
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

data class ScheduleResponse(
    val requirementProgress: RequirementProgress,
    val allocations: List<AllocationResponse>,
    val appointments: List<AppointmentScheduleResponse>
)

data class AllocationResponse(
    val id: Long,
    val project: Project,
    val projectAvailability: ProjectAvailabilityDetails?,
    val frequency: String?,
    val dayOfWeek: String,
    val startDateInclusive: LocalDate?,
    val endDateInclusive: LocalDate?,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val pickUp: PickUp?,
)

data class ProjectAvailabilityDetails(
    val frequency: String?,
    val dayOfWeek: String,
    val startDateInclusive: LocalDate?,
    val endDateExclusive: LocalDate?
)

data class AppointmentScheduleResponse(
    val id: Long,
    val version: UUID,
    val project: CodeName,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val outcome: CodeDescription?,
    val minutesCredited: Long,
    val allocationId: Long?
)

fun UnpaidWorkAllocation.toAllocationResponse() = AllocationResponse(
    id = id,
    project = Project(project),
    projectAvailability = projectAvailability?.toProjectAvailabilityDetails(),
    frequency = requestedFrequency?.description,
    dayOfWeek = allocationDay.weekDay,
    startDateInclusive = startDate,
    endDateInclusive = endDate,
    startTime = startTime,
    endTime = endTime,
    pickUp = PickUp(
        time = pickUpTime,
        location = pickUpLocation?.toPickUpLocation(),
    ),
)

fun UnpaidWorkProjectAvailability.toProjectAvailabilityDetails() = ProjectAvailabilityDetails(
    frequency = frequency?.description,
    dayOfWeek = dayOfWeek.weekDay,
    startDateInclusive = startDate,
    endDateExclusive = endDate
)

fun UnpaidWorkAppointment.toAppointmentScheduleResponse() = AppointmentScheduleResponse(
    id = id,
    version = UUID(rowVersion, contact.rowVersion),
    project = CodeName(project.name, project.code),
    date = date,
    startTime = startTime,
    endTime = endTime,
    outcome = contact.outcome?.toCodeDescription(),
    minutesCredited = minutesCredited ?: 0,
    allocationId = allocation?.id
)
