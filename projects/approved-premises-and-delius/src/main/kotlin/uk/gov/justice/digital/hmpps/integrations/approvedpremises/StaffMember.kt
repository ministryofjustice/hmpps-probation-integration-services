package uk.gov.justice.digital.hmpps.integrations.approvedpremises

data class StaffMember(
    val staffCode: String,
    val staffIdentifier: Long,
    val forenames: String,
    val surname: String,
    val username: String?,
)
