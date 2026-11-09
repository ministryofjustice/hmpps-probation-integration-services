package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class SupervisionPackageResponse(
    val crn: String,
    val eventId: Long,
    val createdDate: ZonedDateTime,
    val supervisionPackageChangedDate: ZonedDateTime?
)
