package uk.gov.justice.digital.hmpps.api.model

data class UnallocatedEventsResponse(
    val crn: String,
    val name: Name,
    val activeEvents: List<ActiveEvent>,
    val licenceConditions: List<LicenceCondition>
)

data class ActiveEvent(
    val eventNumber: String,
    val teamCode: String,
    val providerCode: String
)
