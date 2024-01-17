package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class AllocationResponse(
    val crn: String,
    val name: Name,
    val event: Event,
    val sentence: Sentence?,
    val initialAppointment: InitialAppointment?,
    val court: NamedCourt,
    val type: CaseType = CaseType.UNKNOWN,
    val probationStatus: ProbationStatus,
    val communityPersonManager: Manager?,
    val handoverDate: LocalDate?
)

data class AllocationDemandResponse(val cases: List<AllocationResponse>)
data class NamedCourt(val name: String)
