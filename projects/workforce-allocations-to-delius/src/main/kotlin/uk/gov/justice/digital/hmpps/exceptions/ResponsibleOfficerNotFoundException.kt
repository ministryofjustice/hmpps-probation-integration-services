package uk.gov.justice.digital.hmpps.exceptions

import java.time.ZonedDateTime

class ResponsibleOfficerNotFoundException(personId: Long, dateTime: ZonedDateTime) :
    RuntimeException("Responsible Officer Not Found for $personId at $dateTime")
