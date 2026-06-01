package uk.gov.justice.digital.hmpps.integration

import java.time.LocalDateTime

data class ReferralCompletion(
    val requirementId: String,
    val requirementCompletedAt: LocalDateTime,
    val sourcedFromEntityType: EntityType,
)