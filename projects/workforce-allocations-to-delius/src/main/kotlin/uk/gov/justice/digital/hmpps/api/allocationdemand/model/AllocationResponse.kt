package uk.gov.justice.digital.hmpps.api.allocationdemand.model

import java.time.LocalDate

data class AllocationResponse(
    val crn: String,
    val name: Name,
    val event: Event,
    val sentence: Sentence?,
    val initialAppointment: InitialAppointment?,
    val type: CaseType = CaseType.UNKNOWN,
    val probationStatus: ProbationStatus,
    val communityPersonManager: Manager?
)

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String,
)

data class Event(val number: String, val manager: Manager? = null)
data class Manager(val code: String, val name: Name, val teamCode: String, val grade: String? = null)
data class Sentence(val type: String, val date: LocalDate, val length: String)

data class InitialAppointment(val date: LocalDate)

data class AllocationDemandResponse(val cases: List<AllocationResponse>)

data class ProbationStatus(
    val status: ManagementStatus
) {
    val description = status.description
}

enum class ManagementStatus(
    val description: String
) {
    CURRENTLY_MANAGED("Currently managed"),
    PREVIOUSLY_MANAGED("Previously managed"),
    NEW_TO_PROBATION("New to probation"),
    UNKNOWN("Unknown")
}

enum class CaseType {
    LICENSE, CUSTODY, COMMUNITY, UNKNOWN
}
