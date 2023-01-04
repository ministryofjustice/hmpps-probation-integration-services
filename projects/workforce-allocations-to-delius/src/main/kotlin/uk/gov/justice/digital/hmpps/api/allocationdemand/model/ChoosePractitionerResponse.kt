package uk.gov.justice.digital.hmpps.api.allocationdemand.model

data class ChoosePractitionerResponse(
    val crn: String,
    val name: Name,
    val event: EventNumber,
    val probationStatus: ProbationStatus,
    val communityPersonManager: Manager?,
    val teams: Map<String, List<Manager>>
)

data class EventNumber(val eventNumber: String)
