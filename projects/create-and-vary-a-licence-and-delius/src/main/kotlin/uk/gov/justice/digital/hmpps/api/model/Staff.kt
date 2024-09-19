package uk.gov.justice.digital.hmpps.api.model

data class Staff(
    val id: Long,
    val code: String,
    val name: Name,
    val teams: List<Team>,
    val provider: Provider,
    val username: String?,
    val email: String?,
    val telephoneNumber: String?,
    val unallocated: Boolean,
)

data class PDUHead(
    val name: Name,
    val email: String?
)

data class StaffName(
    val id: Long,
    val name: Name,
    val code: String,
    val username: String?
)

data class StaffEmail(
    val code: String,
    val email: String?
)
