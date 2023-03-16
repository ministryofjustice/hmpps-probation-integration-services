package uk.gov.justice.digital.hmpps.api.model

data class Manager(
    val code: String,
    val name: Name,
    val provider: Provider,
    val team: Team,
    val username: String? = null,
    val email: String? = null
)

data class Name(val forename: String, val middleName: String?, val surname: String)
data class Provider(val code: String, val description: String)
data class Team(val code: String, val description: String, val district: District)
data class District(val code: String, val description: String, val borough: Borough)
data class Borough(val code: String, val description: String)
