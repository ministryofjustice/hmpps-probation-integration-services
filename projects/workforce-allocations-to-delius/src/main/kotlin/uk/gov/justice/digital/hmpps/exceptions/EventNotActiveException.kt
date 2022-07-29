package uk.gov.justice.digital.hmpps.exceptions

class EventNotActiveException(eventId: Long) : RuntimeException("Event $eventId not active")
