package uk.gov.justice.digital.hmpps.api.model

import jakarta.validation.constraints.NotEmpty

data class AllocationImpact(
    val crn: String?,
    val name: Name?,
    val staff: StaffMember?,
)

data class AllocationDetailRequest(val crn: String, val staffCode: String)

data class AllocationDetailRequests(
    @field:NotEmpty val cases: List<AllocationDetailRequest>,
)

data class AllocationDetails(val cases: List<AllocationImpact>)
