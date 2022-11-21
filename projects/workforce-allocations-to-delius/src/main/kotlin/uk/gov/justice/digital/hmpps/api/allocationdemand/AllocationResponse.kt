package uk.gov.justice.digital.hmpps.api.allocationdemand

import com.fasterxml.jackson.annotation.JsonValue
import java.time.LocalDate

data class AllocationResponse(
    val crn: String,
    val name: Name,
    val event: Event,
    val sentence: Sentence?,
    val initialAppointment: InitialAppointment?,
    val type: CaseType = CaseType.UNKNOWN,
    val status: ProbationStatus
)

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String,
)

data class Event(val number: String, val manager: Manager)
data class Manager(val code: String, val name: Name, val teamCode: String, val grade: String? = null)
data class Sentence(val type: String, val date: LocalDate, val length: String)

data class InitialAppointment(val date: LocalDate)

data class AllocationDemandResponse(val cases: List<AllocationResponse>)

data class ProbationStatus(
    val status: ManagementStatus,
    val previousManager: Manager? = null
)

enum class ManagementStatus(
    @JsonValue
    val status: String
) {
    CURRENTLY_MANAGED("Currently managed"),
    PREVIOUSLY_MANAGED("Previously managed"),
    NEW_TO_PROBATION("New to probation")
}

enum class CaseType {
    LICENSE, CUSTODY, COMMUNITY, UNKNOWN
}
