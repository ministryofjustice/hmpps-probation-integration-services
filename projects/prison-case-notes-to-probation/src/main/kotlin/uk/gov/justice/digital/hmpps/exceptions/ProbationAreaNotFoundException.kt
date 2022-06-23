package uk.gov.justice.digital.hmpps.exceptions

class ProbationAreaNotFoundException(code: String) : RuntimeException("Probation area not found: $code")
