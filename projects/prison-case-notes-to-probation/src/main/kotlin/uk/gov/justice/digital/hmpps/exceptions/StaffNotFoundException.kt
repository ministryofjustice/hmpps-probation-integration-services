package uk.gov.justice.digital.hmpps.exceptions

class StaffNotFoundException(code: String) : RuntimeException("Staff not found: $code")