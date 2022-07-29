package uk.gov.justice.digital.hmpps.exceptions

import java.time.ZonedDateTime

class OrderManagerNotFoundException(eventId: Long, dateTime: ZonedDateTime) :
    RuntimeException("Order Manager Not Found for $eventId at $dateTime")
