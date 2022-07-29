package uk.gov.justice.digital.hmpps.exceptions

class EventNotFoundException(eventId: Long) : RuntimeException("Unable to find event with id $eventId")
