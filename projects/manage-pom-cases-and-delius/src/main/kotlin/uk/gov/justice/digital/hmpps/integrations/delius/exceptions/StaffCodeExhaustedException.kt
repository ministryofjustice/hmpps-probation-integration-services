package uk.gov.justice.digital.hmpps.integrations.delius.exceptions

class StaffCodeExhaustedException(code: String) : RuntimeException("Officer codes exhausted for: $code")
