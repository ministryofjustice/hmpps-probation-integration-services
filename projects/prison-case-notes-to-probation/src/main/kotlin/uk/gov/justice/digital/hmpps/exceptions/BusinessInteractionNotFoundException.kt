package uk.gov.justice.digital.hmpps.exceptions

class BusinessInteractionNotFoundException(code: String) :
    RuntimeException("No Business Interaction found with code $code")
