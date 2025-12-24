package uk.gov.justice.digital.hmpps.dto

import uk.gov.justice.digital.hmpps.messaging.Defendant
import java.time.ZonedDateTime

data class InsertRemandDTO(
    val defendant: Defendant,
    val mainOffence: OffenceAndPlea,
    val additionalOffences: List<OffenceAndPlea>,
    val courtCode: String,
    val sittingDay: ZonedDateTime,
    val caseUrn: String,
    val hearingId: String
)