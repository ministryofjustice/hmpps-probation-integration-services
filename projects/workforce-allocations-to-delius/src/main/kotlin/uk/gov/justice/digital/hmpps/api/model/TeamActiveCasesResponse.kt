package uk.gov.justice.digital.hmpps.api.model

data class TeamActiveCasesResponse(
    val code: String,
    val description: String,
    val staff: List<ActiveCasesResponse>
)

