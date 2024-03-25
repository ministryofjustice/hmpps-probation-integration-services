package uk.gov.justice.digital.hmpps.exceptions

class StaffCodeExhaustedException(code: String) : RuntimeException("Officer codes exhausted for: $code")
