package uk.gov.justice.digital.hmpps.integrations.delius

class DeliusValidationError(override val message: String) : RuntimeException(message)
