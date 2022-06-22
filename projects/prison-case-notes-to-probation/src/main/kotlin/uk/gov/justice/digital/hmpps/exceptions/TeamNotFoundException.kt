package uk.gov.justice.digital.hmpps.exceptions

class TeamNotFoundException(code: String) : RuntimeException("Team not found: $code")