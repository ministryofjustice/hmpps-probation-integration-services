package uk.gov.justice.digital.hmpps.integrations.tier

import java.time.ZonedDateTime

data class TierCalculation(
    val tierScore: String,
    val calculationId: String,
    val calculationDate: ZonedDateTime,
)
