package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class AllocationDemandStaffResponse(
    val crn: String,
    val name: Name,
    val staff: StaffMember?,
    val allocatingStaff: StaffMember?,
    val initialAppointment: InitialAppointment?,
    val ogrs: RiskOGRS?,
    val sentence: AllocationDemandSentence?,
    val court: Court?,
    val offences: List<Offence>,
    val activeRequirements: List<Requirement>,
)

data class Requirement(
    val mainCategory: String,
    val subCategory: String?,
    val length: String,
    val id: Long,
)

interface Court {
    val name: String
    val appearanceDate: LocalDate
}

data class AllocationDemandSentence(
    val description: String,
    val code: String,
    val date: ZonedDateTime,
    private val lengthValue: Long,
    private val lengthDescription: String,
) {
    val length: String = "$lengthValue $lengthDescription"
}

interface Offence {
    val mainCategory: String
    val mainOffence: Boolean
}
