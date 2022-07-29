package uk.gov.justice.digital.hmpps.exceptions

class TeamNotActiveException(code: String) : RuntimeException("Team not active: $code")
