package uk.gov.justice.digital.hmpps.exceptions

class LimitedAccessException(val crn: String, val username: String, override val message: String) :
    RuntimeException(message)
