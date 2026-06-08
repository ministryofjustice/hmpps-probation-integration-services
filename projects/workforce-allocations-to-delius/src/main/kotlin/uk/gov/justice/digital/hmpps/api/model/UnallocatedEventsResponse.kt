package uk.gov.justice.digital.hmpps.api.model

data class UnallocatedEventsResponse(
    val crn: String,
    val name: Name,
    val activeEvents: List<ActiveEvent>,
    val licenceConditions: List<UnallocatedLicenceCondition>
)

data class ActiveEvent(
    val eventNumber: String,
    val teamCode: String,
    val providerCode: String
)

data class UnallocatedLicenceCondition(
    val id: Long,
    val mainCategory: String,
    val subCategory: String?,
    val startDate: java.time.LocalDate,
    val commencementDate: java.time.LocalDate?,
    val terminationDate: java.time.LocalDate?,
    val active: Boolean
)
