package uk.gov.justice.digital.hmpps.exceptions

import java.time.ZonedDateTime

class PersonManagerNotFoundException(crn: String, dateTime: ZonedDateTime) :
    RuntimeException("Person Manager Not Found for $crn at $dateTime")
