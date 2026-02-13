package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.contact.toCodeDescription
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAllocation
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointment
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UpwProject
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UpwProjectAvailability
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
    val project: ProjectDetails,
    val projectAvailability: ProjectAvailabilityDetails?,
    val frequency: String?,
    val dayOfWeek: String,
    val startDateInclusive: LocalDate?,
    val endDateInclusive: LocalDate?,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val pickUp: PickUp?,
)

data class ProjectDetails(
    val name: String,
    val code: String,
    val expectedEndDateExclusive: LocalDate?,
    val actualEndDateExclusive: LocalDate?,
    val type: CodeName,
    val provider: CodeName?,
    val team: CodeName?
)

data class ProjectAvailabilityDetails(
    val frequency: String?,
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
    id = this.id,
    project = this.project.toProjectDetails(),
    projectAvailability = this.projectAvailability?.toProjectAvailabilityDetails(),
    frequency = this.requestedFrequency?.description,
    dayOfWeek = this.allocationDay.weekDay,
    startDateInclusive = this.startDate,
    endDateInclusive = this.endDate,
    startTime = this.startTime,
    endTime = this.endTime,
    pickUp = PickUp(
        time = this.pickUpTime,
        location = this.pickUpLocation?.toPickUpLocation(),
    ),
)

fun UpwProject.toProjectDetails() = ProjectDetails(
    name = this.name,
    code = this.code,
    expectedEndDateExclusive = this.expectedEndDate,
    actualEndDateExclusive = this.completionDate,
    type = CodeName(this.projectType.description, this.projectType.code),
    provider = CodeName(this.team.provider.description, this.team.provider.code),
    team = CodeName(this.team.description, this.team.code)
)

fun UpwProjectAvailability.toProjectAvailabilityDetails() = ProjectAvailabilityDetails(
    frequency = this.frequency?.description,
    endDateExclusive = this.endDate
)

fun UnpaidWorkAppointment.toAppointmentScheduleResponse() = AppointmentScheduleResponse(
    id = this.id,
    version = UUID(this.rowVersion, this.contact.rowVersion),
    project = CodeName(this.project.name, this.project.code),
    date = this.date,
    startTime = this.startTime,
    endTime = this.endTime,
    outcome = this.contact.outcome?.toCodeDescription(),
    minutesCredited = this.minutesCredited ?: 0,
    allocationId = this.allocation?.id
)
