package uk.gov.justice.digital.hmpps.model

data class StaffResponse(
    val code: String,
    val name: PersonName,
    val grade: StaffGrade?,
    val keyWorker: Boolean
)

data class PersonName(
    val forename: String,
    val surname: String,
    val middleName: String?
)

data class StaffGrade(
    val code: String,
    val description: String
)

data class ProbationArea(
    val code: String,
    val description: String,
)

data class StaffDetail(
    val email: String?,
    val telephoneNumber: String?,
    val teams: List<Team> = emptyList(),
    val probationArea: ProbationArea,
    val username: String?,
    val name: PersonName,
    val code: String,
    val active: Boolean
)


