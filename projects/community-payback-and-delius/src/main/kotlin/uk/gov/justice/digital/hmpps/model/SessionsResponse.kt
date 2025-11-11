package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.delius.entity.UnpaidWorkSessionDto

data class SessionsResponse(
    val sessions: List<UnpaidWorkSessionDto>
)
