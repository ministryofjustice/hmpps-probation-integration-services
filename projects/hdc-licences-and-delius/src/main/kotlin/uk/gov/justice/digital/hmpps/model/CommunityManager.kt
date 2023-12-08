package uk.gov.justice.digital.hmpps.model

data class CommunityManager(
    val code: String,
    val staffId: Long,
    val name: Name,
    val team: Team,
    val localAdminUnit: LocalAdminUnit,
    val provider: Provider,
    val isUnallocated: Boolean,
)
