package uk.gov.justice.digital.hmpps.model

data class Team(
    val code: String,
    val description: String
)

data class TeamDetails(
    val code: String,
    val description: String,
    val telephone: String?,
    val emailAddress: String?,
    val probationDeliveryUnit: ProbationDeliveryUnit,
    val localAdminUnit: LocalAdminUnit
)
