package uk.gov.justice.digital.hmpps.exceptions

class TransferReasonNotFoundException(code: String) : RuntimeException("Transfer Reason Not Found: $code")
