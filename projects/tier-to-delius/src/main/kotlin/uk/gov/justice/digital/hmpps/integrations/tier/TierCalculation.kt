package uk.gov.justice.digital.hmpps.integrations.tier

import java.time.ZonedDateTime

data class TierCalculationV2(
    val tierScore: String,
    val calculationId: String,
    val calculationDate: ZonedDateTime
)

data class TierCalculationV3(
    val tierScore: String,
    val provisional: Boolean,
    val calculationId: String,
    val calculationDate: ZonedDateTime
)
