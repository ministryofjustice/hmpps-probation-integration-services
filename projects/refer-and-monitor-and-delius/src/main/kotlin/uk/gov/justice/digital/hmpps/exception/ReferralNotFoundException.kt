package uk.gov.justice.digital.hmpps.exception

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReferralNotFoundException(
    referralUrn: String,
    crn: String,
    eventId: Long,
    date: LocalDate,
    reason: String
) : UnprocessableException(
    "ReferralNotFound",
    mapOf(
        "referralUrn" to referralUrn,
        "crn" to crn,
        "eventId" to eventId.toString(),
        "date" to date.format(DateTimeFormatter.ISO_LOCAL_DATE),
        "reason" to reason
    )
)
