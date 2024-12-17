package uk.gov.justice.digital.hmpps.api.model.risk

import java.time.ZonedDateTime

data class Opd(
    val eligible: Boolean,
    val date: ZonedDateTime,
)
