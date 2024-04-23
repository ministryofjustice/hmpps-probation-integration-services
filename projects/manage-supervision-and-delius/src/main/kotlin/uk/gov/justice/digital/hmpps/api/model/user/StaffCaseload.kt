package uk.gov.justice.digital.hmpps.api.model.user

import uk.gov.justice.digital.hmpps.api.model.Name

data class StaffCaseload(
    val provider: String?,
    val staff: Name,
    val caseload: List<StaffCase>
)
