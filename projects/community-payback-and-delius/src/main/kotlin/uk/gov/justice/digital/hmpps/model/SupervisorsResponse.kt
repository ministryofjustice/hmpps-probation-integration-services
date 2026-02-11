package uk.gov.justice.digital.hmpps.model

data class SupervisorsResponse(
    val supervisors: List<Supervisor>
)

data class Supervisor(
    val name: Name,
    val code: String,
    val grade: CodeDescription?,
    val unallocated: Boolean
)

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String
)