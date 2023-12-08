package uk.gov.justice.digital.hmpps.api

data class Manager(
    val code: String,
    val name: Name,
    val team: Team,
    val email: String?,
    val telephone: String?,
) {
    val unallocated = code.endsWith("U")
}

data class Name(val forename: String, val surname: String)

data class Team(val code: String, val description: String, val email: String?, val telephone: String?)
