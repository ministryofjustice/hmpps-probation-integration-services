package uk.gov.justice.digital.hmpps.api.model.personalDetails

data class ProbationPractitioner(
    val code: String,
    val provider: Provider,
    val team: Team,
    val username: String?
) {
    data class Provider(val code: String, val description: String)
    data class Team(val code: String, val description: String)
}