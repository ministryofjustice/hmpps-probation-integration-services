package uk.gov.justice.digital.hmpps.integrations.delius.offender

import java.time.ZonedDateTime

data class OffenderEvent(
    val offenderId: Long,
    val crn: String,
    val nomsNumber: String?,
    val sourceId: Long,
    val eventDatetime: ZonedDateTime,
)
