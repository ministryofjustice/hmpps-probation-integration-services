package uk.gov.justice.digital.hmpps.api.model.personalDetails

data class ProbationPractitioner(
    val code: String,
    val name: Name,
    val provider: Provider,
    val team: Team,
    val unallocated: Boolean,
    val username: String?
) {
    data class Name(val forename: String, val surname: String)
    data class Provider(val code: String, val name: String)
    data class Team(val code: String, val description: String)
}