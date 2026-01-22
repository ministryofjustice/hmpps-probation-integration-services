package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkSession

data class SessionsResponse(
    val sessions: List<UnpaidWorkSession>
)
