package uk.gov.justice.digital.hmpps.integration

import java.time.LocalDateTime

data class ReferralCompletion(
    val licReqId: String,
    val licReqCompletedAt: LocalDateTime,
    val sourcedFromEntityType: EntityType,
)