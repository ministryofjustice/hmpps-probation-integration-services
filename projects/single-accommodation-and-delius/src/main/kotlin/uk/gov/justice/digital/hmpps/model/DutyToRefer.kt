package uk.gov.justice.digital.hmpps.model

import java.util.*

data class DutyToRefer(
    val id: UUID,
    val crn: String,
    val viewUrl: String,
)
