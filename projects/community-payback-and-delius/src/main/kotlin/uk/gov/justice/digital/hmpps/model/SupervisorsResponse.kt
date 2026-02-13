package uk.gov.justice.digital.hmpps.model

data class SupervisorsResponse(
    val supervisors: List<Supervisor>
)

data class Supervisor(
    val name: StaffName,
    val code: String,
    val grade: CodeDescription?,
    val unallocated: Boolean
)