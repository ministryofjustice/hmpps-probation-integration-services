package uk.gov.justice.digital.hmpps.dto

import uk.gov.justice.digital.hmpps.messaging.Defendant
import uk.gov.justice.digital.hmpps.messaging.HearingOffence
import java.time.ZonedDateTime

data class InsertRemandDTO (
    val defendant: Defendant,
    val courtCode: String,
    val hearingOffence: HearingOffence,
    val sittingDay: ZonedDateTime,
    val caseUrn: String,
    val hearingId: String
)