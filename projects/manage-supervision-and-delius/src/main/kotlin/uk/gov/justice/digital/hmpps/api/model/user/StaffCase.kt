package uk.gov.justice.digital.hmpps.api.model.user

import uk.gov.justice.digital.hmpps.api.model.Name

data class StaffCase(
    val caseName: Name,
    val crn: String
)
