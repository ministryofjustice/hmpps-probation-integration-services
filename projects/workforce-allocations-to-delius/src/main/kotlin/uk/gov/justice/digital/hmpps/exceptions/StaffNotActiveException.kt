package uk.gov.justice.digital.hmpps.exceptions

class StaffNotActiveException(code: String) : RuntimeException("Staff not active: $code")
