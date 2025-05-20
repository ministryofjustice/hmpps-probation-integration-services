package uk.gov.justice.digital.hmpps.api.model

data class ProbationEstateResponse(
    val providers: List<ProviderWithProbationDeliveryUnits>
)

data class ProviderWithProbationDeliveryUnits(
    val code: String,
    val description: String,
    val probationDeliveryUnits: List<ProbationDeliveryUnitWithLocalAdminUnits>
)

data class ProbationDeliveryUnitWithLocalAdminUnits(
    val code: String,
    val description: String,
    val localAdminUnits: List<LocalAdminUnitWithTeams>
)

data class LocalAdminUnitWithTeams(
    val code: String,
    val description: String,
    val teams: List<Team>
)