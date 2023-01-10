package uk.gov.justice.digital.hmpps.api.model

data class AllocationImpact(
    val crn: String,
    val name: Name,
    val staff: StaffMember
)