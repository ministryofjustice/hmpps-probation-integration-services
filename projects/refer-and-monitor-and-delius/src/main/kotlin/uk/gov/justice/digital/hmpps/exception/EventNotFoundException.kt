package uk.gov.justice.digital.hmpps.exception

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EventNotFoundException(
    referralUrn: String,
    crn: String,
    eventId: Long,
    date: LocalDate
) : UnprocessableException(
    "Event Not Found for Referral",
    listOfNotNull(
        "referralUrn" to referralUrn,
        "crn" to crn,
        "eventId" to eventId.toString(),
        "date" to date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    ).toMap()
)
