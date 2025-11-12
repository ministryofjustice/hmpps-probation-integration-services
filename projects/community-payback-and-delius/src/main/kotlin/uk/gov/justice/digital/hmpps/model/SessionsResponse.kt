package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.delius.entity.UnpaidWorkSession

data class SessionsResponse(
    val sessions: List<UnpaidWorkSession>
)
