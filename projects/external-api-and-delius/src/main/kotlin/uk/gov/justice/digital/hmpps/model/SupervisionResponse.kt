package uk.gov.justice.digital.hmpps.model

data class SupervisionResponse(
    val communityManager: Manager,
    val mappaDetail: MappaDetail?,
    val supervisions: List<Supervision>
)

data class Manager(
    val code: String,
    val name: Name,
    val username: String?,
    val email: String?,
    val telephoneNumber: String?,
    val team: Team,
) {
    val allocated = !code.endsWith("U")
}

data class Name(val forename: String, val surname: String)
data class Team(
    val code: String,
    val description: String,
    val email: String?,
    val telephoneNumber: String?,
    val provider: Provider
)

data class Provider(val code: String, val description: String)
