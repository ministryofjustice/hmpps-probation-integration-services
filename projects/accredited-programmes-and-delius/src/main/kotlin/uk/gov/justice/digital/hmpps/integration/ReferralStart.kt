package uk.gov.justice.digital.hmpps.integration

import java.time.LocalDateTime

data class ReferralStart(
    val licReqId: String,
    val licReqStartedAt: LocalDateTime,
    val sourcedFromEntityType: EntityType,
)
