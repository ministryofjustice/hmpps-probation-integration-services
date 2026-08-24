package uk.gov.justice.digital.hmpps.controller.model

data class CommunityOffenderManager(
    val crn: String,
    val communityManager: CommunityManager
)

data class CommunityManager(
    val jobRole: String?,
    val emailAddress: String?,
    val pdu: String,
    val officeName: String?,
    val name: Name,
    val teamPhoneNumber: String?
)

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String,
)
