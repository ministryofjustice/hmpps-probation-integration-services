package uk.gov.justice.digital.hmpps.api.model

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

data class AllocationDemandResponse(val cases: List<AllocationResponse>)
