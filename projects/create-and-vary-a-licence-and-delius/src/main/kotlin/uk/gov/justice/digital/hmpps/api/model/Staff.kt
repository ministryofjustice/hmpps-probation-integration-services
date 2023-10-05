package uk.gov.justice.digital.hmpps.api.model

data class Staff(
    val code: String,
    val name: Name,
    val teams: List<Team>,
    val username: String?,
    val email: String?,
    val unallocated: Boolean
)

data class PDUHead(
    val name: Name,
    val email: String?
)
